package de.olafj.springws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Component
@ServerEndpoint(value = "/webSocket/{username}",
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class)
public class Socket {

    private Session session;

    private static final Set<Socket> chatEndpoints = new CopyOnWriteArraySet<>();

    private static final Map<String, String> users = Collections.synchronizedMap(new HashMap<>());

    private static final Logger LOG = LoggerFactory.getLogger(Socket.class);

    public Socket() {

    }

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException {

        this.session = session;
        chatEndpoints.add(this);
        users.put(session.getId(), username);

        LOG.debug("new session (id: {}) for user: {}", session.getId(), username);

        Message message = new Message();
        message.setFrom(session.getId());
        message.setContent("Connected!");
        broadcast(message);
    }

    @OnMessage
    public void onMessage(Session session, Message message)
            throws IOException {

        message.setFrom(session.getId());

        LOG.debug("new message from session (id: {}): {}", session.getId(), message);

        broadcast(message);
    }

    @OnClose
    public void onClose(Session session) throws IOException {

        chatEndpoints.remove(this);

        var username = users.get(session.getId());

        users.remove(session.getId());

        LOG.debug("closed session (id: {}) for user: {}", session.getId(), username);

        Message message = new Message();
        message.setFrom(session.getId());
        message.setContent("Disconnected!");
        broadcast(message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }

    private static void broadcast(Message message) {

        var userSessions = users.entrySet()
                .stream().filter(sessionIdToUserName -> sessionIdToUserName.getValue().equals(message.getTo()))
                .map(Map.Entry::getKey).collect(Collectors.toList());

        chatEndpoints.stream().filter(socket -> userSessions.contains(socket.session.getId())).forEach(socket -> {
            synchronized (socket) {
                try {
                    LOG.debug("sending message for {} via session {} # {}", message.getTo(), socket.session.getId(), message);
                    socket.session.getBasicRemote().sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
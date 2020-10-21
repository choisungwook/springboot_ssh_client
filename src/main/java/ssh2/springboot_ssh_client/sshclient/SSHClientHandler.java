package ssh2.springboot_ssh_client.sshclient;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface SSHClientHandler {

    public void initConnection(WebSocketSession session);
    public void recvHandle(String buffer, WebSocketSession session);
    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException;
    public void close(WebSocketSession session);
}

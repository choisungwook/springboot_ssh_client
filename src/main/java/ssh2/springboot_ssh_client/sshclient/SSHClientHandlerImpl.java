package ssh2.springboot_ssh_client.sshclient;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public class SSHClientHandlerImpl implements SSHClientHandler{
    @Override
    public void initConnection(WebSocketSession session) {

    }

    @Override
    public void recvHandle(String buffer, WebSocketSession session) {

    }

    @Override
    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {

    }

    @Override
    public void close(WebSocketSession session) {

    }
}

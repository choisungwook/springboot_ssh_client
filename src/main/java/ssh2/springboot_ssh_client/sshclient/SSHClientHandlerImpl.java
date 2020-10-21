package ssh2.springboot_ssh_client.sshclient;

import com.jcraft.jsch.JSch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;
import ssh2.springboot_ssh_client.websocket.ConstantPool;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SSHClientHandlerImpl implements SSHClientHandler{
    private static Map<String, Object> sshMap = new ConcurrentHashMap<>();

    @Override
    public void initConnection(WebSocketSession session) {
        JSch jSch = new JSch();
        SSHConnectionInfo sshConnectionInfo = new SSHConnectionInfo();
        sshConnectionInfo.setJSch(jSch);
        sshConnectionInfo.setSession(session);
        
        // 웹소켓 세션과 ssh정보 저장
        String uuid = String.valueOf(session.getAttributes().get(ConstantPool.USER_UUID_KEY));
        sshMap.put(uuid, sshConnectionInfo);
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

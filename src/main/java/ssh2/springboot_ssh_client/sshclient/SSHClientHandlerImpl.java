package ssh2.springboot_ssh_client.sshclient;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;
import ssh2.springboot_ssh_client.controller.dao.Requset_connect_SSHServer_DAO;
import ssh2.springboot_ssh_client.websocket.ConstantPool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
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

    /***
     * ssh서버 연결 후 계속 응답 수신
     * @param sshConnectionInfo
     * @param request
     * @param webSocketSession
     * @throws JSchException
     */
    private void connect_to_SSHServer(SSHConnectionInfo sshConnectionInfo, Requset_connect_SSHServer_DAO request, WebSocketSession webSocketSession) throws JSchException, IOException {
        // == ssh 세션 설정 ==//
        Session session = null;
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session = sshConnectionInfo.getJSch().getSession(request.getUsername(), request.getHost(), request.getPort());
        session.setConfig(config);
        session.setPassword(request.getPassword());

        // ssh 채널 생성
        Channel channel = session.openChannel("shell");
        sshConnectionInfo.setChannel(channel);

        // == ssh서버의 명령어 실행 결과를 수신 ==//
        InputStream inputStream = channel.getInputStream();
        try{
            byte[] buffer = new byte[1024];
            int readsize = 0;
            while((readsize = inputStream.read(buffer)) != -1){
                sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, readsize));
            }
        }finally {
            session.disconnect();
            channel.disconnect();
            if(inputStream != null){
                inputStream.close();
            }
        }
    }

    /***
     * SSH 서버에 명령어 전달
     * @param channel
     * @param command
     */
    private void sendCommand_to_SSHServer(Channel channel, String command) throws IOException {
        if(channel != null){
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();
        }
    }
}

package ssh2.springboot_ssh_client.sshclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ssh2.springboot_ssh_client.websocket.dao.Requset_connect_SSHServer_DAO;
import ssh2.springboot_ssh_client.websocket.ConstantPool;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SSHClientHandlerImpl implements SSHClientHandler{
    private static Map<String, Object> sshMap = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private PipedInputStream pinWrapper = new PipedInputStream(4096);
    private PipedOutputStream pout;

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
        ObjectMapper objectMapper = new ObjectMapper();
        Requset_connect_SSHServer_DAO request = null;

        try {
            request = objectMapper.readValue(buffer, Requset_connect_SSHServer_DAO.class);
        } catch (JsonProcessingException e) {
            log.error("[-] request's translation task is error " + e.getMessage());
        }

        String userId = String.valueOf(session.getAttributes().get(ConstantPool.USER_UUID_KEY));
        // 처음 클라이언트 요청시 ssh연결 작업 수행
        if(ConstantPool.WEB_SSH_OPERATE_CONNECT.equals(request.getOperator())){
            SSHConnectionInfo sshConnectionInfo = (SSHConnectionInfo) sshMap.get(userId);
            Requset_connect_SSHServer_DAO final_request = request;

            // 계속 ssh서버 실행결과를 수신하기 위해 쓰레드로 실행
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        connect_to_SSHServer(sshConnectionInfo, final_request, session);
                    } catch (JSchException | IOException e) {
                        log.error("[-] connecting SSHServer is error: " + e.getMessage());
                        close(session);
                    }
                }
            });
        }else if(ConstantPool.WEB_SSH_OPERATE_COMMAND.equals(request.getOperator())){ // 명령어 실행 요청
            String command = request.getCommand();
            SSHConnectionInfo sshConnectionInfo = (SSHConnectionInfo) sshMap.get(userId);
            if(sshConnectionInfo != null){
                try {
                    sendCommand_to_SSHServer(sshConnectionInfo.getChannel(), command);
                } catch (IOException e) {
                    log.error("[-] Sending Command to SSHServer is error: " + e);
                    close(session);
                }
            }
        }else{ // 존재하지 않은 동작 요청
            log.error("[-] Unknown requset command");
            close(session);
        }
    }

    /***
     * 웹 소켓에 ssh응답 전달
     * @param session
     * @param buffer
     * @throws IOException
     */
    @Override
    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {
        session.sendMessage(new TextMessage(buffer));
    }

    @Override
    public void close(WebSocketSession session) {
        String userId = String.valueOf(session.getAttributes().get(ConstantPool.USER_UUID_KEY));
        SSHConnectionInfo sshConnectionInfo = (SSHConnectionInfo) sshMap.get(userId);

        if(sshConnectionInfo != null){
            if(sshConnectionInfo.getChannel() != null){
                sshConnectionInfo.getChannel().disconnect();
            }
            sshMap.remove(userId);
        }
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
        Session session = sshConnectionInfo.getJSch().getSession(request.getUsername(), request.getHost(), request.getPort());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword(request.getPassword());
        session.connect();

        // ssh 채널 생성
        Channel channel = session.openChannel("shell");
        channel.connect();
        sshConnectionInfo.setChannel(channel);

        sendCommand_to_SSHServer(channel, "\r");

        // == ssh서버의 명령어 실행 결과를 수신 ==//
        InputStream inputStream = channel.getInputStream();

        try{
            byte[] buffer = new byte[1024];
            int readsize = 0;

            while((readsize = inputStream.read(buffer)) !=  -1){
                sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, readsize));
            }

        } finally {
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
            command = command + "\r";
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }
}

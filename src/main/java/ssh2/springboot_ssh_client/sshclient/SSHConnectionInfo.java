package ssh2.springboot_ssh_client.sshclient;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

/***
 * 다수의 SSH연결을 위한 정보저장 클래스
 */
@Getter
@Setter
public class SSHConnectionInfo {
    private WebSocketSession session;
    private JSch jSch;
    private Channel channel;
}

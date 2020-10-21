package ssh2.springboot_ssh_client.controller.dao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Requset_connect_SSHServer_DAO {
    private String operator; // 동작모드
    private String host;
    private int port;
    private String username;
    private String password;
    private String command;
}

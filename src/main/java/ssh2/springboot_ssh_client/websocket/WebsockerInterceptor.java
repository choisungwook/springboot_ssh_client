package ssh2.springboot_ssh_client.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class WebsockerInterceptor implements HandshakeInterceptor {
    /***
     * 핸드쉐이크 전, 세션을 구분할 고유 UUID를 설정
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if(request instanceof ServletServerHttpRequest){
            ServletServerHttpRequest httpRequest =(ServletServerHttpRequest)request;

            String uuid = UUID.randomUUID().toString().replace("-","");
            log.info("[*] uuid: " + uuid);
            attributes.put(ConstantPool.USER_UUID_KEY, uuid);
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}

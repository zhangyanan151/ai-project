package cn.techwolf.server.model;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Data
public class LoginUser {
    private String email;
    private boolean isOperator;
}
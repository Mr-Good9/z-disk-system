package com.good.zdisksystem.entity.param;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class UpdateUserParam {

    private String nickname;

    private String email;

    private String verifyCode;

    private String phone;

}

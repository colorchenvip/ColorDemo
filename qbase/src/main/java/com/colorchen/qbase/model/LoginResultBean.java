package com.colorchen.qbase.model;

import android.text.TextUtils;

/**
 * Created by zhaojieb9 on 2017/7/10.
 */

public class LoginResultBean {
  private String token;

  private UserBean user;
  private String name;

  public String getToken() {
    if (TextUtils.isEmpty(token)) return "";
    return token;
  }

  public UserBean getUser() {
    return user;
  }
}

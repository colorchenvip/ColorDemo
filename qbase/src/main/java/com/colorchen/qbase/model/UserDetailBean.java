package com.colorchen.qbase.model;

import android.text.TextUtils;

/**
 * Created by zhaojieb9 on 2017/7/11.
 */

public class UserDetailBean {
  private String id;
  private String eip;
  private String email;
  private String uid;
  private String name;
  private String birthday;
  private String sex;
  private String mobile;
  private String telephone;
  private String street;
  private String isEnabled;
  private String code;
  private String codename;
  private String codepath;
  private String namepath;
  private String positionCode;
  private String positionName;

  public String getBirthday() {
    if (TextUtils.isEmpty(birthday)) return "";
    return birthday;
  }

  public void setBirthday(String birthday) {
    this.birthday = birthday;
  }

  public String getCodepath() {
    return codepath;
  }

  public void setCodepath(String codepath) {
    this.codepath = codepath;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getSex() {
    if (TextUtils.isEmpty(sex)) return "";
    return sex;
  }

  public void setSex(String sex) {
    this.sex = sex;
  }

  public String getMobile() {
    if (TextUtils.isEmpty(mobile)) return "";
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getTelephone() {
    return telephone;
  }

  public void setTelephone(String telephone) {
    this.telephone = telephone;
  }

  public String getEip() {
    return eip;
  }

  public void setEip(String eip) {
    this.eip = eip;
  }

  public String getNamepath() {
    return namepath;
  }

  public void setNamepath(String namepath) {
    this.namepath = namepath;
  }

  public String getUid() {
    if (TextUtils.isEmpty(uid)) return "";
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getStreet() {
    if (TextUtils.isEmpty(street)) return "";
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getCodename() {
    return codename;
  }

  public void setCodename(String codename) {
    this.codename = codename;
  }

  public String getName() {
    if (TextUtils.isEmpty(name)) return "";
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPositionName() {
    return positionName;
  }

  public void setPositionName(String positionName) {
    this.positionName = positionName;
  }

  public String getEmail() {
    if (TextUtils.isEmpty(email)) return "";
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getIsEnabled() {
    if (TextUtils.isEmpty(isEnabled)) return "";
    return isEnabled;
  }

  public void setIsEnabled(String isEnabled) {
    this.isEnabled = isEnabled;
  }

  public String getPositionCode() {
    if (TextUtils.isEmpty(positionCode)) return "";
    return positionCode;
  }

  public void setPositionCode(String positionCode) {
    this.positionCode = positionCode;
  }

}

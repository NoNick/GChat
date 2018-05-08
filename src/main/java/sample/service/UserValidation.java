package sample.service;

import sample.model.User;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public interface UserValidation {
    User getValidUser(String name, String hash) throws UnsupportedEncodingException, NoSuchAlgorithmException;
}

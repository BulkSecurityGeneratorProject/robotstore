package com.rbc.ace.web.rest.vm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * View Model object for storing the user's key and password.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyAndPasswordVM {

    private String key;

    private String newPassword;
}

/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.authn.realm;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

/**
 * Password matcher for BCrypt
 *
 * @opensearch.experimental
 */
public class BCryptPasswordMatcher implements CredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {

        UsernamePasswordToken userToken = (UsernamePasswordToken) token;
        String password = new String(userToken.getPassword());
        char[] credentials = ((String) info.getCredentials()).toCharArray();
        String hashed = new String(credentials);
        return OpenBSDBCrypt.checkPassword(hashed, password.toCharArray());
    }

}
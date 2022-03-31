package com.awpghost.auth.dto.requests;

import com.awpghost.auth.validators.ValidEmail;
import com.awpghost.auth.validators.ValidPassword;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class AuthEmailDto {
    @NotNull
    @NotEmpty
    @ValidEmail
    private String email;

    @ValidPassword
    @NotNull
    @NotEmpty
    private String password;

    private Boolean rememberMe;

    public AuthEmailDto() {
    }

    public @NotNull @NotEmpty String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public Boolean getRememberMe() {
        return this.rememberMe;
    }

    public void setEmail(@NotNull @NotEmpty String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AuthEmailDto)) return false;
        final AuthEmailDto other = (AuthEmailDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$email = this.getEmail();
        final Object other$email = other.getEmail();
        if (this$email == null ? other$email != null : !this$email.equals(other$email)) return false;
        final Object this$password = this.getPassword();
        final Object other$password = other.getPassword();
        if (this$password == null ? other$password != null : !this$password.equals(other$password)) return false;
        final Object this$rememberMe = this.getRememberMe();
        final Object other$rememberMe = other.getRememberMe();
        if (this$rememberMe == null ? other$rememberMe != null : !this$rememberMe.equals(other$rememberMe))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AuthEmailDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $email = this.getEmail();
        result = result * PRIME + ($email == null ? 43 : $email.hashCode());
        final Object $password = this.getPassword();
        result = result * PRIME + ($password == null ? 43 : $password.hashCode());
        final Object $rememberMe = this.getRememberMe();
        result = result * PRIME + ($rememberMe == null ? 43 : $rememberMe.hashCode());
        return result;
    }

    public String toString() {
        return "AuthEmailDto(email=" + this.getEmail() + ", password=" + this.getPassword() + ", rememberMe=" + this.getRememberMe() + ")";
    }
}

package com.milkroad.exception;

public class CancelamentoInvalidoException extends RuntimeException {
    public CancelamentoInvalidoException(String mensagem) {
        super(mensagem);
    }
}

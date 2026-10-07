package com.example.demo.exception;

public class ListaNoVaciaException extends RuntimeException {
    public ListaNoVaciaException(String mensaje) {
        super(mensaje);
    }
}

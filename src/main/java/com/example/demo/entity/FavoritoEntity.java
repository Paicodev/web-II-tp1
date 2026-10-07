package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

//Entity es un anotador que define a la clase como entidad
@Entity
//Table es un anotador para que la clase se identifique como tabla de nombre favoritos
@Table(name = "favoritos")
public class FavoritoEntity {

    //Anotador para identificar a la columna como primary key
    @Id
    //GeneratedValue indica que el valor se genera automaticamente
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Anotador para identificar la columna como producto_id
    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    //Anotador para identificar la columna como nota
    @Column(name = "nota")
    private String nota;

    //Anotador para identificar la columna como fecha_alta
    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_id")
    private ListaEntity lista;

//constructor vacio para persistencia
    public FavoritoEntity() {

    }

//constructor completo para creación manual
    public FavoritoEntity(Long id, Long productoId, String nota, LocalDateTime fechaAlta) {
        this.id = id;
        this.productoId = productoId;
        this.nota = nota;
        this.fechaAlta = fechaAlta;
    }

//getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public ListaEntity getLista() {
        return lista;
    }

    public void setLista(ListaEntity lista) {
        this.lista = lista;
    }
}

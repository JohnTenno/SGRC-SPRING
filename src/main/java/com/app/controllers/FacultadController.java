package com.app.controllers;

import com.app.models.Facultad;
import com.app.repositories.FacultadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/facultades")
public class FacultadController {

    @Autowired
    private FacultadRepository facultadRepository;

    // Este método escucha cuando alguien entra a la ruta por internet
    @GetMapping
    public List<Facultad> obtenerTodasLasFacultades() {
        // Usa el repositorio mágico para ir a MySQL, traer todas las facultades y devolverlas
        return facultadRepository.findAll();
    }
}
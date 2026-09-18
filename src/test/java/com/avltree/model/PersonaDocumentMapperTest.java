package com.avltree.model;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PersonaDocumentMapperTest {

    private PersonaDocumentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PersonaDocumentMapper();
    }

    @Test
    void elIdentificadorSeDerivaDelDpi() {
        Persona ana = new Persona("Ana", "López", 30, "1234567890123");

        assertEquals("persona_1234567890123", mapper.id(ana));
    }

    @Test
    void elIdentificadorNoDependeDeLosDemasCampos() {
        Persona antes = new Persona("Ana", "López", 30, "1234567890123");
        Persona despues = new Persona("Ana María", "López Ruiz", 31, "1234567890123");

        assertEquals(mapper.id(antes), mapper.id(despues),
                "Cambiar nombre o edad no debe mover el registro de sitio");
    }

    @Test
    @DisplayName("Dos DPIs distintos nunca comparten identificador")
    void identificadoresUnicosParaDpisDistintos() {
        // Con identificadores basados en hashCode existían colisiones reales
        // (p.ej. los DPIs 7572515731943 y 4086513132424 compartían hash) y una
        // persona sobrescribía a la otra en la base de datos.
        Random random = new Random(20260918L);
        Set<String> dpis = new HashSet<>();
        Set<String> identificadores = new HashSet<>();

        for (int i = 0; i < 200_000; i++) {
            StringBuilder dpi = new StringBuilder();
            for (int digito = 0; digito < 13; digito++) {
                dpi.append((char) ('0' + random.nextInt(10)));
            }
            dpis.add(dpi.toString());
            identificadores.add(mapper.id(Persona.claveDe(dpi.toString())));
        }

        assertEquals(dpis.size(), identificadores.size(),
                "Cada DPI distinto debe producir un identificador distinto");
    }

    @Test
    void colisionConocidaDeHashCodeYaNoProduceElMismoIdentificador() {
        Persona una = new Persona("Ana", "López", 30, "7572515731943");
        Persona otra = new Persona("Beto", "Ruiz", 40, "4086513132424");

        assertEquals(una.hashCode(), otra.hashCode(),
                "Estos dos DPIs comparten hashCode: es justo el caso que rompía la persistencia");
        assertNotEquals(mapper.id(una), mapper.id(otra),
                "Aun compartiendo hashCode deben tener identificadores distintos");
    }

    @Test
    void rechazaPersonasSinDpi() {
        assertThrows(IllegalArgumentException.class, () -> mapper.id(null));
        assertThrows(IllegalArgumentException.class, () -> mapper.id(new Persona()));
        assertThrows(IllegalArgumentException.class, () -> mapper.id(Persona.claveDe("   ")));
    }

    @Test
    void conversionDeIdaYVuelta() {
        Persona original = new Persona("Ana", "López", 30, "1234567890123");

        Document documento = mapper.toDocument(original);
        Persona recuperada = mapper.fromDocument(documento);

        assertEquals(original, recuperada);
        assertEquals(original.getNombre(), recuperada.getNombre());
        assertEquals(original.getEdad(), recuperada.getEdad());
    }
}

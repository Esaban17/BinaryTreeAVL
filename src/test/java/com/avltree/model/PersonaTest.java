package com.avltree.model;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonaTest {

    private Persona persona(String dpi) {
        return new Persona("Ana", "López", 30, dpi);
    }

    @Test
    void ordenaPorDpi() {
        assertTrue(persona("11111111").compareTo(persona("22222222")) < 0);
        assertTrue(persona("22222222").compareTo(persona("11111111")) > 0);
        assertEquals(0, persona("11111111").compareTo(persona("11111111")));
    }

    @Test
    @DisplayName("compareTo tolera un DPI null en vez de lanzar NullPointerException")
    void compareToConDpiNullNoLanza() {
        Persona sinDpi = new Persona("Sin", "Dpi", 20, null);
        Persona conDpi = persona("11111111");

        assertDoesNotThrow(() -> sinDpi.compareTo(conDpi));
        assertDoesNotThrow(() -> conDpi.compareTo(sinDpi));
        assertTrue(sinDpi.compareTo(conDpi) < 0, "Un DPI ausente se ordena primero");
        assertTrue(conDpi.compareTo(sinDpi) > 0);
        assertEquals(0, sinDpi.compareTo(new Persona("Otro", "Nombre", 40, null)));
    }

    @Test
    void compareToConNullDevuelvePositivo() {
        assertTrue(persona("11111111").compareTo(null) > 0);
    }

    @Test
    @DisplayName("La igualdad y el hash dependen sólo del DPI")
    void igualdadPorDpi() {
        Persona a = new Persona("Ana", "López", 30, "11111111");
        Persona b = new Persona("Otro", "Nombre", 55, "11111111");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, persona("22222222"));
        assertNotEquals(a, null);
        assertNotEquals(a, "no soy una persona");
    }

    @Test
    void claveDeCreaUnaPersonaBuscable() {
        Persona clave = Persona.claveDe("11111111");

        assertEquals("11111111", clave.getDpi());
        assertEquals(0, clave.compareTo(persona("11111111")));
    }

    @Test
    void validaDatosCorrectos() {
        Persona valida = persona("12345678");

        assertTrue(valida.isValid());
        assertTrue(valida.validationErrors().isEmpty());
    }

    @Test
    void reportaCadaErrorDeValidacion() {
        Persona invalida = new Persona("  ", "", 0, "123");
        List<String> errores = invalida.validationErrors();

        assertFalse(invalida.isValid());
        assertEquals(4, errores.size(), "Se esperaban 4 errores, se obtuvo: " + errores);
        assertTrue(errores.stream().anyMatch(e -> e.contains("nombre")));
        assertTrue(errores.stream().anyMatch(e -> e.contains("apellido")));
        assertTrue(errores.stream().anyMatch(e -> e.contains("edad")));
        assertTrue(errores.stream().anyMatch(e -> e.contains("DPI")));
    }

    @Test
    void rechazaEdadesFueraDeRango() {
        assertFalse(new Persona("Ana", "López", 0, "12345678").isValid());
        assertFalse(new Persona("Ana", "López", -5, "12345678").isValid());
        assertFalse(new Persona("Ana", "López", 150, "12345678").isValid());
        assertTrue(new Persona("Ana", "López", 149, "12345678").isValid());
        assertTrue(new Persona("Ana", "López", 1, "12345678").isValid());
    }

    @Test
    void rechazaDpiDemasiadoCorto() {
        assertFalse(new Persona("Ana", "López", 30, "1234567").isValid());
        assertTrue(new Persona("Ana", "López", 30, "12345678").isValid());
    }

    @Test
    void serializaYDeserializaSinPerderDatos() {
        Persona original = new Persona("Ana", "López", 30, "12345678");

        Persona recuperada = Persona.fromDocument(original.toDocument());

        assertEquals(original.getNombre(), recuperada.getNombre());
        assertEquals(original.getApellido(), recuperada.getApellido());
        assertEquals(original.getEdad(), recuperada.getEdad());
        assertEquals(original.getDpi(), recuperada.getDpi());
        assertEquals(original, recuperada);
    }

    @Test
    void fromDocumentConNullDevuelveNull() {
        assertNull(Persona.fromDocument(null));
    }

    @Test
    @DisplayName("Un documento sin DPI falla en voz alta en vez de perderse en silencio")
    void fromDocumentSinDpiLanzaExcepcion() {
        Document sinDpi = new Document("nombre", "Ana").append("apellido", "López").append("edad", 30);

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> Persona.fromDocument(sinDpi));
        assertTrue(error.getMessage().contains("DPI"));
    }

    @Test
    void fromDocumentSinEdadUsaCero() {
        Document doc = new Document("nombre", "Ana").append("apellido", "López").append("dpi", "12345678");

        assertEquals(0, Persona.fromDocument(doc).getEdad());
    }

    @Test
    void representacionesDeTexto() {
        Persona ana = new Persona("Ana", "López", 30, "12345678");

        assertEquals("Ana López", ana.getNombreCompleto());
        assertEquals("Ana López (12345678)", ana.toShortString());
        assertTrue(ana.getDetalle().contains("12345678"));
        assertTrue(ana.getDetalle().contains("30"));
        assertTrue(ana.toString().contains("12345678"));
    }

    @Test
    void nombreCompletoToleraCamposNull() {
        assertEquals("", new Persona().getNombreCompleto());
        assertEquals("Ana", new Persona("Ana", null, 30, "12345678").getNombreCompleto());
    }

    @Test
    void seOrdenaCorrectamenteEnUnaLista() {
        List<Persona> personas = Arrays.asList(persona("33333333"), persona("11111111"), persona("22222222"));
        personas.sort(Persona::compareTo);

        assertEquals("11111111", personas.get(0).getDpi());
        assertEquals("22222222", personas.get(1).getDpi());
        assertEquals("33333333", personas.get(2).getDpi());
    }
}

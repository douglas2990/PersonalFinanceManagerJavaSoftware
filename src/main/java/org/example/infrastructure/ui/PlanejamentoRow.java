package org.example.infrastructure.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class PlanejamentoRow {
    private final String categoria;
    private final DoubleProperty metaAnual = new SimpleDoubleProperty();
    private final DoubleProperty[] meses = new DoubleProperty[12];

    public PlanejamentoRow(String categoria) {
        this.categoria = categoria;
        for (int i = 0; i < 12; i++) meses[i] = new SimpleDoubleProperty(0.0);
    }

    // Getters e Setters (Essenciais para o JavaFX)
    public String getCategoria() { return categoria; }
    public DoubleProperty metaAnualProperty() { return metaAnual; }
    public DoubleProperty mesProperty(int index) { return meses[index]; }
}

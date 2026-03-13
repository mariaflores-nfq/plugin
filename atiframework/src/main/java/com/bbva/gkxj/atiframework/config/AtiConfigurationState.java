package com.bbva.gkxj.atiframework.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Servicio de persistencia de estado para la configuración de ATI.
 * Almacena: entorno de BBDD, UUAA seleccionada, usuario y branch activa.
 * Accesible desde Settings → Tools → ATI Framework → ATI Configuration.
 */
@Service(Service.Level.APP)
@State(name = "AtiConfigurationState", storages = @Storage("atiConfiguration.xml"))
public final class AtiConfigurationState implements PersistentStateComponent<AtiConfigurationState.State> {

    private State myState = new State();

    public static AtiConfigurationState getInstance() {
        return ApplicationManager.getApplication().getService(AtiConfigurationState.class);
    }

    /**
     * Estado persistente con los valores de configuración ATI.
     */
    public static class State {
        /** Entorno de BBDD: "Integrado" o "Preproducción" */
        public String atiBbdd = "Integrado";
        /** UUAA seleccionada */
        public String uuaa = "";
        /** Usuario seleccionado */
        public String usuarioATI = System.getProperty("user.name");
        /** Usuario seleccionado */
        public String usuarioIntegrado = "XAGKXJ1I";
        /** Usuario seleccionado */
        public String usuarioPreproduccion = "XAGKXJ1P";
        /** Usuario seleccionado */
        public String passwordIntegrado = "x4gXzlI1";
        /** Usuario seleccionado */
        public String passwordPreproduccion = "RohEsbZDBNF2";
        /** Branch activa */
        public String branch = "";
        /** Host de MongoDB para Integrado */
        public String mongoHostIntegrado = "limdb601:30042";
        /** Base de datos MongoDB para Integrado */
        public String mongoDbIntegrado = "BGKXJ001";
        /** Host de MongoDB para Preproducción */
        public String mongoHostPreproduccion = "lwmdb601:30042,lwmdb602:30042,lwmdb603:30042";
        /** Base de datos MongoDB para Preproducción */
        public String mongoDbPreproduccion = "BGKXJ001";
    }

    @Override
    public @Nullable State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    // ---- Getters de conveniencia ----

    public String getAtiBbdd() {
        return myState.atiBbdd;
    }

    public void setAtiBbdd(String value) {
        myState.atiBbdd = value;
    }

    public String getUuaa() {
        return myState.uuaa;
    }

    public void setUuaa(String value) {
        myState.uuaa = value;
    }

    public String getUsuario() {
        return "Preproducción".equals(myState.atiBbdd)
                ? myState.usuarioPreproduccion
                : myState.usuarioIntegrado;
    }

    public String getUsuarioAti() {
        return myState.usuarioATI;
    }

    public void setUsuarioAti(String value) {
        myState.usuarioATI = value;
    }

    public String getPassword() {
        return "Preproducción".equals(myState.atiBbdd)
                ? myState.passwordPreproduccion
                : myState.passwordIntegrado;
    }

    public String getBranch() {
        return myState.branch;
    }

    public void setBranch(String value) {
        myState.branch = value;
    }

    /**
     * Obtiene el host de MongoDB según el entorno seleccionado.
     */
    public String getMongoHost() {
        return "Preproducción".equals(myState.atiBbdd)
                ? myState.mongoHostPreproduccion
                : myState.mongoHostIntegrado;
    }

    /**
     * Obtiene el nombre de la base de datos según el entorno seleccionado.
     */
    public String getMongoDatabase() {
        return "Preproducción".equals(myState.atiBbdd)
                ? myState.mongoDbPreproduccion
                : myState.mongoDbIntegrado;
    }
}


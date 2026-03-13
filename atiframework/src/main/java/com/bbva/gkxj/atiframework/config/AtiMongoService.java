package com.bbva.gkxj.atiframework.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.MongoTimeoutException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Servicio que gestiona la conexión a MongoDB de ATI y proporciona
 * métodos para obtener UUAAs, usuarios y branches.
 */
@Service(Service.Level.APP)
public final class AtiMongoService {

    private static final Logger LOG = Logger.getInstance(AtiMongoService.class);

    /** Nombre de la colección que almacena las UUAAs disponibles */
    private static final String UUAA_COLLECTION = "CGKXJ_CFG_UUAA";
    /** Nombre de la colección que almacena los usuarios */
    private static final String USERS_COLLECTION = "users";
    /** Nombre de la colección que almacena las branches */
    private static final String BRANCHES_COLLECTION = "branches";

    public static AtiMongoService getInstance() {
        return ApplicationManager.getApplication().getService(AtiMongoService.class);
    }

    /**
     * Crea un MongoClient con la configuración del entorno seleccionado,
     * incluyendo autenticación con usuario y contraseña.
     *
     * @param host   Host(s) de MongoDB (puede ser múltiples separados por coma, ej: "host1:port1,host2:port2")
     * @param dbName Nombre de la base de datos (usado como authSource)
     * @param user   Usuario de MongoDB
     * @param password Contraseña de MongoDB
     * @return MongoClient configurado con autenticación
     */
    private MongoClient createClient(String host, String dbName, String user, String password) {
        // Parsear múltiples hosts separados por coma
        List<ServerAddress> hosts = Arrays.stream(host.split(","))
                .map(String::trim)
                .filter(h -> !h.isEmpty())
                .map(h -> {
                    String[] parts = h.split(":");
                    if (parts.length == 2) {
                        return new ServerAddress(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                    }
                    return new ServerAddress(parts[0].trim());
                })
                .collect(Collectors.toList());

        MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(hosts)
                                .serverSelectionTimeout(5, TimeUnit.SECONDS))
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(5, TimeUnit.SECONDS)
                                .readTimeout(5, TimeUnit.SECONDS));

        // Configurar credenciales si usuario y contraseña están disponibles
        if (user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
            MongoCredential credential = MongoCredential.createCredential(user, dbName, password.toCharArray());
            settingsBuilder.credential(credential);
            LOG.info("ATI MongoDB: Conectando con autenticación, usuario=" + user + ", authSource=" + dbName);
        } else {
            LOG.warn("ATI MongoDB: Conectando SIN autenticación (usuario o contraseña no configurados)");
        }

        return MongoClients.create(settingsBuilder.build());
    }

    /**
     * Prueba la conexión a MongoDB con la configuración actual.
     *
     * @param host     Host de MongoDB
     * @param dbName   Nombre de la base de datos
     * @return true si la conexión es exitosa
     */
    public boolean testConnection(String host, String dbName) {
        AtiConfigurationState state = AtiConfigurationState.getInstance();
        String user = state.getUsuario();
        String password = state.getPassword();

        try (MongoClient client = createClient(host, dbName, user, password)) {
            MongoDatabase db = client.getDatabase(dbName);
            // Intentar listar las colecciones como test de conexión
            db.listCollectionNames().first();
            return true;
        } catch (MongoTimeoutException e) {
            LOG.warn("Timeout al conectar a MongoDB: " + host, e);
            return false;
        } catch (Exception e) {
            LOG.warn("Error al conectar a MongoDB: " + host, e);
            return false;
        }
    }

    /**
     * Obtiene las UUAAs disponibles desde la base de datos de ATI.
     *
     * @return Lista de UUAAs disponibles ordenadas alfabéticamente
     */
    public List<String> getUuaas() {
        AtiConfigurationState state = AtiConfigurationState.getInstance();
        String host = state.getMongoHost();
        String dbName = state.getMongoDatabase();

        List<String> uuaas = new ArrayList<>();
        try (MongoClient client = createClient(host, dbName, state.getUsuario(), state.getPassword())) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(UUAA_COLLECTION);
            Document filter = new Document("userList.userId", state.getUsuarioAti());
            for (Document doc : collection.find(filter)) {
                String name = doc.getString("uuaa");
                if (name != null && !name.isEmpty()) {
                    uuaas.add(name);
                }
            }
            Collections.sort(uuaas);
        } catch (Exception e) {
            LOG.warn("Error al obtener UUAAs desde MongoDB", e);
        }
        return uuaas;
    }

    /**
     * Obtiene los usuarios disponibles para una UUAA concreta.
     *
     * @param uuaa Código de la UUAA
     * @return Lista de usuarios disponibles para la UUAA
     */
    public List<String> getUsersForUuaa(String uuaa) {
        if (uuaa == null || uuaa.isEmpty()) {
            return Collections.emptyList();
        }

        AtiConfigurationState state = AtiConfigurationState.getInstance();
        String host = state.getMongoHost();
        String dbName = state.getMongoDatabase();

        List<String> users = new ArrayList<>();
        try (MongoClient client = createClient(host, dbName, state.getUsuario(), state.getPassword())) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(USERS_COLLECTION);

            Document filter = new Document("uuaa", uuaa);
            for (Document doc : collection.find(filter)) {
                String username = doc.getString("username");
                if (username != null && !username.isEmpty()) {
                    users.add(username);
                }
            }
            Collections.sort(users);
        } catch (Exception e) {
            LOG.warn("Error al obtener usuarios para UUAA: " + uuaa, e);
        }
        return users;
    }

    /**
     * Comprueba si un usuario existe para una UUAA determinada.
     *
     * @param uuaa     Código de la UUAA
     * @param username Nombre del usuario
     * @return true si el usuario existe en la UUAA
     */
    public boolean userExistsForUuaa(String uuaa, String username) {
        if (uuaa == null || uuaa.isEmpty() || username == null || username.isEmpty()) {
            return false;
        }

        AtiConfigurationState state = AtiConfigurationState.getInstance();
        String host = state.getMongoHost();
        String dbName = state.getMongoDatabase();

        try (MongoClient client = createClient(host, dbName, state.getUsuario(), state.getPassword())) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(USERS_COLLECTION);

            Document filter = new Document("uuaa", uuaa).append("username", username);
            return collection.find(filter).first() != null;
        } catch (Exception e) {
            LOG.warn("Error al comprobar usuario " + username + " en UUAA " + uuaa, e);
            return false;
        }
    }

    /**
     * Obtiene las branches disponibles para una UUAA concreta.
     *
     * @param uuaa Código de la UUAA
     * @return Lista de branches disponibles para la UUAA
     */
    public List<String> getBranchesForUuaa(String uuaa) {
        if (uuaa == null || uuaa.isEmpty()) {
            return Collections.emptyList();
        }

        AtiConfigurationState state = AtiConfigurationState.getInstance();
        String host = state.getMongoHost();
        String dbName = state.getMongoDatabase();

        List<String> branches = new ArrayList<>();
        try (MongoClient client = createClient(host, dbName, state.getUsuario(), state.getPassword())) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(BRANCHES_COLLECTION);

            Document filter = new Document("uuaa", uuaa);
            for (Document doc : collection.find(filter)) {
                String branchName = doc.getString("name");
                if (branchName != null && !branchName.isEmpty()) {
                    branches.add(branchName);
                }
            }
            Collections.sort(branches);
        } catch (Exception e) {
            LOG.warn("Error al obtener branches para UUAA: " + uuaa, e);
        }
        return branches;
    }

    /**
     * Obtiene la branch activa para un usuario en una UUAA concreta.
     *
     * @param uuaa     Código de la UUAA
     * @param username Nombre del usuario
     * @return Nombre de la branch activa, o cadena vacía si no se encuentra
     */
    public String getActiveBranchForUser(String uuaa, String username) {
        if (uuaa == null || uuaa.isEmpty() || username == null || username.isEmpty()) {
            return "";
        }

        AtiConfigurationState state = AtiConfigurationState.getInstance();
        String host = state.getMongoHost();
        String dbName = state.getMongoDatabase();

        try (MongoClient client = createClient(host, dbName, state.getUsuario(), state.getPassword())) {
            MongoDatabase db = client.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(USERS_COLLECTION);

            Document filter = new Document("uuaa", uuaa).append("username", username);
            Document userDoc = collection.find(filter).first();

            if (userDoc != null) {
                String activeBranch = userDoc.getString("activeBranch");
                return activeBranch != null ? activeBranch : "";
            }
        } catch (Exception e) {
            LOG.warn("Error al obtener branch activa para usuario " + username + " en UUAA " + uuaa, e);
        }
        return "";
    }
}


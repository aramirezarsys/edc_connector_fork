package arsys.extension.download.pull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.edc.spi.monitor.Monitor;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

@Consumes({ MediaType.APPLICATION_JSON }) // La clase solo puede recibir peticiones de tipo application/json
@Produces({ MediaType.APPLICATION_JSON }) // La clase solo puede devolver peticiones de tipo application/json
@Path("/")
public class PullController {

    private final Monitor monitor; // Es el logger, se lo pasa la clase Extension

    public PullController(Monitor monitor) {
        this.monitor = monitor;
    }

    @POST // Para obtener peticiones POST, tambien pueden ser GET, PUT, PATCH...
    @Path("pull")
    public String endppoint(String body) {// Creamos un endpoint que recibe un JSON como cadena de texto
        String response = "correct";
        ObjectMapper mapper;
        JsonNode jsonNode;
        HttpsURLConnection con = null;
        long empiezaTransferencia = 0;
        monitor.info("Hemos entrado en el metodo Pull");
        try {
            empiezaTransferencia = System.currentTimeMillis();
            mapper = new ObjectMapper();
            jsonNode = mapper.readTree(body);// Aquí creamos los nodos del JSON
            // Ahora le quitamos las comillas a los campos del JSON que queremos utilizar,
            // ya que si no hiciesemos esto los valores con las comillas no serían utilies
            String endpoint = jsonNode.get("endpoint").toString().replaceAll("\"", "");
            monitor.info("Apuntamos al endpoint: " + endpoint);
            String authCode = jsonNode.get("authCode").toString().replaceAll("\"", "");
            String id = jsonNode.get("id").toString().replaceAll("\"", "");

            monitor.info("El valor del auth code es: " + authCode);
            // Accedemos al endpoint que nos ha proporcionado el proveedor en el JSON con el
            // authcode como cabecera de la petición, aceptando input
            URL url = new URL(endpoint);

            con = (HttpsURLConnection) url.openConnection(); // Abrimos la conexion en un objeto

            con.setDoInput(true); // Activamos la opción del input
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Length", authCode.getBytes().length + ""); // Funciona sin este header, pero
                                                                                       // hace que funcione un poco mas rápido
            con.setRequestProperty("Authorization", authCode);
            con.setRequestProperty("x-api-key", "password");

            // Aquí utilizo un DataInputStream para poder leer datos binarios
            try (DataInputStream rd = new DataInputStream(con.getInputStream());
                    OutputStream fos = new FileOutputStream("./" + id)) {
                fos.write(rd.readAllBytes()); // Leemos la fuente de datos externa y la escribimos en el fichero interno
                fos.flush();
            } catch (Exception ignored) {
                monitor.severe("Excepción leyendo o escribiendo los datos", ignored);
            }
        } catch (JsonProcessingException e) {
            monitor.severe("El body no era un JSON con el formato correcto" + Arrays.toString(e.getStackTrace()));
            response = "processing error";
        } catch (MalformedURLException e) {
            response = "No se ha podido leer la conexion";
            throw new RuntimeException(e);
        } catch (IOException e) {
            response = "No se ha podido escribir en el disco";
            throw new RuntimeException(e);
        } finally {
            monitor.info("Transferencia terminada, ha tardado: " + (System.currentTimeMillis() - empiezaTransferencia)
                    + " ms");
            if (con != null)
                con.disconnect(); // Cerramos la conexión
        }
        // Si ha llegado hasta aquí y no ha saltado excepción devuelvo que ha sido
        // correcto
        return """
                {
                    "response":""" + response + """
                }
                    """;
    }
}
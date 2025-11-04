import { useEffect, useState } from "react";
import axios from "axios";

export default function Historial() {
  const [archivos, setArchivos] = useState([]);

  const cargarHistorial = async () => {
    try {
      const res = await axios.get("http://localhost:8080/historial");
      setArchivos(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    cargarHistorial();
  }, []);

  return (
    <div>
      <h2>Historial de archivos</h2>
      {archivos.length === 0 ? (
        <p>No hay archivos cargados aún.</p>
      ) : (
        <ul>
          {archivos.map((a, i) => (
            <li key={i}>
              <strong>{a.nombre}</strong> (versión {a.version}) —{" "}
              <a href={a.url} target="_blank" rel="noreferrer">
                Ver archivo
              </a>
            </li>
          ))}
        </ul>
      )}
      <button onClick={cargarHistorial} style={{ marginTop: "1rem" }}>
        🔄 Actualizar
      </button>
    </div>
  );
}

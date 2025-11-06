import { useState } from "react";
import axios from "axios";

export default function UploadFile() {
  const [file, setFile] = useState(null);
  const [message, setMessage] = useState("");

  const handleUpload = async () => {
    if (!file) {
      setMessage("Selecciona un archivo antes de subirlo.");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      const res = await axios.post("http://localhost:8080/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      setMessage("Se subio el archivo correctamente.");
    } catch (err) {
      console.error(err);
      setMessage("Error al subir el archivo.");
    }
  };

  return (
    <div>
      <h2>Subir archivo</h2>
      <input
        type="file"
        onChange={(e) => setFile(e.target.files[0])}
        style={{ marginRight: "1rem" }}
      />
      <button onClick={handleUpload}>Subir</button>
      <p style={{ marginTop: "1rem" }}>{message}</p>
    </div>
  );
}

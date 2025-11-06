// frontend/src/api.js
import axios from 'axios';

// ✅ Usa la variable de entorno (si existe) o el nombre del servicio Docker del backend
const API_URL = import.meta.env.VITE_API_URL || 'http://cloudbox_app:4567';

// Sube un archivo al backend
export const subirArchivo = async (file) => {
  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await axios.post(`${API_URL}/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  } catch (error) {
    console.error('❌ Error al subir el archivo:', error.message);
    throw error;
  }
};

// Obtiene el historial de archivos
export const obtenerHistorial = async () => {
  try {
    const response = await axios.get(`${API_URL}/historial`);
    return response.data;
  } catch (error) {
    console.error('❌ Error al obtener el historial:', error.message);
    throw error;
  }
};

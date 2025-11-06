import UploadFile from "./UploadFile";
import Historial from "./Historial";

export default function App() {
  return (
    <div style={{
      padding: "2rem",
      fontFamily: "Segoe UI, sans-serif",
      backgroundColor: "#4a8df3ff",
      minHeight: "100vh"
    }}>
      <h1 style={{ color: "#2d3748", marginBottom: "2rem" }}>CloudBox☁️</h1>
      <UploadFile />
      <hr style={{ margin: "2rem 0" }} />
      <Historial />
    </div>
  );
}

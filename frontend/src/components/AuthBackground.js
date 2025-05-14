export default function AuthBackground({ children }) {
    return (
        <div className="container-fluid d-flex justify-content-center align-items-center px-3" style={{ minHeight: "100vh" }}>
            <div className="card shadow rounded p-4 border-0" style={{ width: "100%", maxWidth: "450px", minHeight: "450px" }}>
                {children}
            </div>
        </div>
    );
}
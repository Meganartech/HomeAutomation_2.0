export default function AuthLayout({ children, customStyle }) {
    return (
        <div className="container-fluid d-flex justify-content-center align-items-center bg-eaeaea px-0" style={{ minHeight: "100vh" }}>
            <div className="card shadow rounded p-4 border-0 mx-auto" style={customStyle}>
                {children}
            </div>
        </div>
    );
};
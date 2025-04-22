import React, { useState } from "react";
import { Link } from "react-router-dom";
import { FaEye, FaEyeSlash } from "react-icons/fa"; 

export default function Login() {
    const [formData, setFormData] = useState({ email: "", password: "" });
    const [showPassword, setShowPassword] = useState(false);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
    };

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    return (
        <>
            <div className="container d-flex justify-content-center align-items-center" style={{ minHeight: "100vh" }}>
                <div className="card shadow border-0 p-4 mx-auto" style={{ maxWidth: "450px", width: "100%", minHeight: "450px" }}>
                    <h3 className="mb-3">Login</h3>
                    <p className="text-muted mb-3">Please fill your detail to access your account.</p>

                    <form onSubmit={handleSubmit}>
                        <div className="form-floating mb-3">
                            <input type="email" name="email" className="form-control mb-1" placeholder="" value={formData.email} onChange={handleChange} required />
                            <label className="text-6c757d">Email</label>
                        </div>

                        <div className="mb-3 position-relative">
                            <div className="form-floating">
                                <input type={showPassword ? "text" : "password"} name="password" className="form-control" placeholder="" value={formData.password} onChange={handleChange} required />
                                <label className="text-6c757d">Password</label>
                            </div>
                            <span
                                onClick={togglePasswordVisibility}
                                style={{ position: "absolute", right: "15px", top: "60%", transform: "translateY(-50%)", cursor: "pointer", color: "#6c757d" }}>
                                {showPassword ? <FaEyeSlash /> : <FaEye />}
                            </span>
                        </div>

                        <div className="text-end mb-3">
                            <Link className="text-danger" to="/forgot/password">Forgot Password?</Link>
                        </div>

                        <div className="mb-3">
                            <button type="submit" className="btn btn-dark w-100">Sign in</button>
                        </div>
                    </form>

                    <p className="text-center text-muted">
                        Don’t have an account?{" "}
                        <Link className="btn btn-link border-0 p-0 mb-1 text-dark" to={"/register"}>Register</Link>
                    </p>
                </div>
            </div>
        </>
    );
};
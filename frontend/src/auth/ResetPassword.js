import React, { useState } from "react";
import { FaEye, FaEyeSlash } from "react-icons/fa";

export default function ResetPassword() {
    const [formData, setFormData] = useState({ password: "", confirmPassword: "" });
    const [showPassword, setShowPassword] = useState(false);

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
    };

    return (
        <>
            <div className="container d-flex justify-content-center align-items-center" style={{ minHeight: "100vh" }}>
                <div className="card shadow border-0 p-4 mx-auto" style={{ maxWidth: "450px", width: "100%", minHeight: "450px" }}>
                    <h3 className="mb-3">Reset Password</h3>
                    <p className="text-muted mb-3">Please enter your password and confirm the password.</p>

                    <form onSubmit={handleSubmit}>
                        <div className="form-floating mb-3">
                            <input type="password" name="password" className="form-control mb-1" placeholder="" value={formData.password} onChange={handleChange} required />
                            <label className="text-6c757d">Password</label>
                        </div>

                        <div className="mb-3 position-relative">
                            <div className="form-floating">
                                <input type={showPassword ? "text" : "password"} name="confirmPassword" className="form-control" placeholder="" value={formData.confirmPassword} onChange={handleChange} required />
                                <label className="text-6c757d">Confirm Password</label>
                            </div>
                            <span
                                onClick={togglePasswordVisibility}
                                style={{ position: "absolute", right: "15px", top: "60%", transform: "translateY(-50%)", cursor: "pointer", color: "#6c757d" }}>
                                {showPassword ? <FaEyeSlash /> : <FaEye />}
                            </span>
                        </div>

                        <div className="mt-7 mb-3">
                            <button type="submit" className="btn btn-dark w-100">Save</button>
                        </div>
                    </form>

                </div>
            </div>
        </>
    );
};
import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import { FaEye, FaEyeSlash } from "react-icons/fa";

export default function Login() {
    const [formData, setFormData] = useState({ email: "", password: "" });
    const [showPassword, setShowPassword] = useState(false);
    const navigate = useNavigate();

    const formField = [
        { name: "email", type: "email", placeholder: "", label: "Email" },
        { name: "password", type: showPassword ? "text" : "password", placeholder: "", label: "Password" }
    ];

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const response = await axios.post('http://localhost:8081/user/login', formData);
            if (response.status === 200) {
                alert(response.data.message);
                navigate('/login');
            }
        } catch (error) {
            if (error.response?.data?.error) {
                alert(`${error.response.data.error}`);
                console.log(error.response.data.error);
            } else {
                alert('Login failed due to server error.');
            }
        }
        setFormData({
            email: '',
            password: ''
        });
    };

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    return (
        <>
            <div className="container-fluid d-flex justify-content-center align-items-center bg-eceaea" style={{ minHeight: "100vh" }}>
                <div className="card shadow border-0 p-4 mx-auto" style={{ maxWidth: "450px", width: "100%", minHeight: "450px" }}>
                    <h3 className="mb-3">Login</h3>
                    <p className="text-muted mb-3">Please fill your detail to access your account.</p>

                    <form onSubmit={handleSubmit}>
                        {formField.map((ref, index) => (
                            <div key={index} className="mb-3 position-relative">
                                <div className="form-floating">
                                    <input type={ref.type} name={ref.name} className="form-control" placeholder={ref.placeholder} value={formData[ref.name]} onChange={handleChange} required />
                                    <label className="text-6c757d">{ref.label}</label>
                                    {ref.name === "password" && (
                                        <span
                                            onClick={togglePasswordVisibility}
                                            style={{ position: "absolute", right: "15px", top: "60%", transform: "translateY(-50%)", cursor: "pointer", color: "#6c757d" }}>
                                            {showPassword ? <FaEyeSlash /> : <FaEye />}
                                        </span>
                                    )}
                                </div>
                            </div>
                        ))}

                        <div className="text-end mb-3">
                            <Link className="text-danger" to="/forgot/password">Forgot Password?</Link>
                        </div>

                        <div className="mb-3">
                            <button type="submit" className="btn btn-dark w-100">Sign in</button>
                        </div>
                    </form>

                    <p className="text-center text-muted">
                        Don't have an account?{" "}
                        <Link className="btn btn-link border-0 p-0 mb-1 text-dark" to={"/register"}>Register</Link>
                    </p>
                </div>
            </div>
        </>
    );
};
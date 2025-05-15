import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import { FaEye, FaEyeSlash } from "react-icons/fa";
import AuthBackground from "../components/AuthBackground";

export default function Login() {

    const [formData, setFormData] = useState({ email: "", password: "" });
    const [showPassword, setShowPassword] = useState(false);
    const navigate = useNavigate();

    const formField = [
        { name: "email", type: "email", label: "Email", autoComplete: "email" },
        { name: "password", type: showPassword ? "text" : "password", label: "Password", autoComplete: "new-password" }
    ];

    const resetForm = () => {
        setFormData({ email: '', password: '' });
    }

    const toggle = () => {
        setShowPassword(!showPassword);
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const { data, status } = await axios.post('http://localhost:8081/user/login', formData);
            if (status === 200) {
                localStorage.setItem('token', data.token);
                localStorage.setItem('role', data.role);
                alert('Login successful!');
                navigate('/settings/profile_');
            }
        } catch (err) {
            if (err.data?.error) {
                alert(`${err.data.error}`);
                console.log(err.data.error);
            } else {
                alert('Login failed due to server error.');
            }
        } finally {
            resetForm();
        }
    };

    return (
        <>
            <AuthBackground>
                
                <h3 className="mb-3">Login</h3>

                <p className="text-muted mb-3">Please fill your detail to access your account.</p>

                <form onSubmit={handleSubmit}>
                    {formField.map((ref, index) => (
                        <React.Fragment key={index}>
                            <div className="position-relative mb-3">
                                <label className="text-6c757d" htmlFor={ref.name}>{ref.label}</label>
                                <input className="form-control" type={ref.type} id={ref.name} name={ref.name} value={formData[ref.name]} onChange={handleChange} autoComplete={ref.autoComplete} required />
                                {ref.name === "password" && (
                                    <span onClick={toggle} style={{ position: "absolute", right: "15px", top: "65%", transform: "translateY(-50%)", cursor: "pointer", color: "#6c757d" }}>
                                        {showPassword ? <FaEyeSlash /> : <FaEye />}
                                    </span>
                                )}
                            </div>
                        </React.Fragment>
                    ))}

                    <div className="text-end mb-3">
                        <Link className="text-danger" to={"/forgot/password"}>Forgot Password?</Link>
                    </div>

                    <div className="mb-3">
                        <button type="submit" className="btn btn-dark w-100">Sign in</button>
                    </div>
                </form>

                <p className="text-center text-muted mb-3">
                    Don't have an account?{" "}
                    <Link className="btn btn-link p-0 border-0 mb-1 text-dark" to={"/register"}>Register</Link>
                </p>

            </AuthBackground>
        </>
    );
};
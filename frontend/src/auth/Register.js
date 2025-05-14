import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { FaEye, FaEyeSlash } from "react-icons/fa";

export default function Register() {

    const [formData, setFormData] = useState({ name: '', email: '', mobileNumber: '', password: '', confirmPassword: '' });
    const [showPassword, setShowPassword] = useState({ password: false, confirmPassword: false });
    const navigate = useNavigate();

    const formField = [
        { name: "name", type: "text", label: "Name", autoComplete: "name" },
        { name: "email", type: "email", label: "Email", autoComplete: "email" },
        { name: "mobileNumber", type: "tel", label: "Contact No", autoComplete: "tel" },
        { name: "password", label: "Password", autoComplete: "new-password" },
        { name: "confirmPassword", label: "Confirm Password", autoComplete: "new-password" },
    ];

    const resetForm = () => {
        setFormData({ name: '', email: '', mobileNumber: '', password: '', confirmPassword: '' });
    }

    const toggle = (field) => {
        setShowPassword({ ...showPassword, [field]: !showPassword[field] });
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (formData.password !== formData.confirmPassword) {
            alert("Passwords do not match!");
            return;
        }

        try {
            const { data, status } = await axios.post('http://localhost:8081/user/register', formData);
            if (status === 200) {
                alert(data.message);
                navigate('/login');
            }
        } catch (err) {
            if (err.data?.error) {
                alert(`${err.data.error}`);
                console.log(err.data.error);
            } else {
                alert('Registration failed due to server error.');
            }
        } finally {
            resetForm();
        }
    };

    return (
        <>
            <div className="container-fluid d-flex justify-content-center align-items-center bg-eceaea px-0" style={{ minHeight: "100vh" }}>
                <div className="card shadow border-0 p-4 mx-auto" style={{ maxWidth: '500px', width: '100%' }}>

                    <h3 className="mb-3">Register</h3>

                    <form onSubmit={handleSubmit}>
                        {formField.map((ref, index) => (
                            <React.Fragment key={index}>
                                <div className="position-relative mb-3">
                                    <label className="text-6c757d" htmlFor={ref.name}>{ref.label}</label>
                                    <input className="form-control" type={(ref.name === "password" || ref.name === "confirmPassword") ? (showPassword[ref.name] ? "text" : "password") : ref.type} id={ref.name} name={ref.name} value={formData[ref.name]} onChange={handleChange} autoComplete={ref.autoComplete} required />
                                    {(ref.name === "password" || ref.name === "confirmPassword") && (
                                        <span onClick={() => toggle(ref.name)} style={{ position: "absolute", right: "15px", top: "65%", transform: "translateY(-50%)", cursor: "pointer", color: "#6c757d" }}>
                                            {showPassword[ref.name] ? <FaEyeSlash /> : <FaEye />}
                                        </span>
                                    )}
                                </div>
                            </React.Fragment>
                        ))}

                        <div className="mb-3">
                            <button type="submit" className="btn btn-dark w-100">Register</button>
                        </div>
                    </form>

                    <p className="text-center text-muted mb-3">
                        By registering you agree to <strong>Terms & Conditions</strong> and <strong>Privacy Policy</strong> of the Vermo
                    </p>

                </div>
            </div>
        </>
    );
};
import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { FaEye, FaEyeSlash } from "react-icons/fa";

export default function Register() {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        mobileNumber: '',
        password: '',
        confirmPassword: ''
    });
    const [showPassword, setShowPassword] = useState(false);
    const navigate = useNavigate();

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
            const response = await axios.post('http://localhost:8081/user/register', formData);
            if (response.status === 200) {
                alert('Registration successful!');
                navigate('/login');
            }
        } catch (error) {
            if (error.response?.data?.error) {
                alert(`${error.response.data.error}`);
                console.log(error.response.data.error);
            } else {
                alert('Registration failed due to server error.');
            }
        }
        setFormData({
            name: '',
            email: '',
            mobileNumber: '',
            password: '',
            confirmPassword: ''
        });
    };

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    return (
        <>
            <div className="container-fluid d-flex justify-content-center align-items-center bg-eceaea" style={{ minHeight: "100vh" }}>
                <div className="card shadow border-0 p-4 mx-auto" style={{ maxWidth: '500px' }}>
                    <h3 className="mb-3">Register</h3>
                    <form onSubmit={handleSubmit}>
                        <div className="form-floating mb-3">
                            <input type="text" name="name" className="form-control mb-1" placeholder='' value={formData.name} onChange={handleChange} required />
                            <label className='text-6c757d'>Name</label>
                        </div>
                        <div className="form-floating mb-3">
                            <input type="email" name="email" className="form-control mb-1" placeholder='' value={formData.email} onChange={handleChange} required />
                            <label className='text-6c757d'>Email</label>
                        </div>
                        <div className="form-floating mb-3">
                            <input type="tel" name="mobileNumber" className="form-control mb-1" placeholder='' value={formData.mobileNumber} onChange={handleChange} required />
                            <label className='text-6c757d'>Mobile Number</label>
                        </div>
                        <div className="form-floating mb-3">
                            <input type="password" name="password" className="form-control mb-1" placeholder='' value={formData.password} onChange={handleChange} required />
                            <label className='text-6c757d'>Password</label>
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
                        <div className='d-flex justify-content-center mt-4 mb-3'>
                            <button type="submit" className="btn btn-dark w-75">Register</button>
                        </div>
                    </form>
                    <p className="text-center text-muted">
                        By registering you agree to <strong>Terms & Conditions</strong> and <strong>Privacy Policy</strong> of the Vermo
                    </p>
                </div>
            </div>
        </>
    );
};
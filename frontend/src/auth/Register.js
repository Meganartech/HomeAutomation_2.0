import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

export default function Register() {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        mobileNumber: '',
        password: '',
        confirmPassword: ''
    });
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
    
        const { name, email, mobileNumber, password } = formData;
        const requestData = { name, email, mobileNumber, password };
    
        try {
            const response = await axios.post('http://localhost:8081/user/register', requestData);
            if (response.status === 200) {
                alert('Registration successful!');
                navigate('/login');
            }
        } catch (error) {
            if (error.response?.data?.error) {
                alert(`Registration failed: ${error.response.data.error}`);
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
    

    return (
        <>
            <div className="container mt-4">
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
                        <div className="form-floating mb-3">
                            <input type="password" name="confirmPassword" className="form-control mb-1" placeholder='' value={formData.confirmPassword} onChange={handleChange} required />
                            <label className='text-6c757d'>Confirm Password</label>
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
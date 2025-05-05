import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { FaEye, FaEyeSlash } from "react-icons/fa";

export default function ChangePasswordContent() {
    const [formData, setFormData] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
    const [editField, setEditField] = useState(null);
    const [showPassword, setShowPassword] = useState({
        currentPassword: false,
        newPassword: false,
        confirmPassword: false
    });
    const navigate = useNavigate();

    const userData = useSelector((state) => state.user);

    const formFields = [
        { name: "currentPassword", placeholder: "Current Password", label: "Current Password" },
        { name: "newPassword", placeholder: "New Password", label: "New Password" },
        { name: "confirmPassword", placeholder: "Confirm Password", label: "Confirm Password" }
    ];

    const handleEditClick = (field) => {
        setEditField(field);
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleProfileSubmit = async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/user/login');
            return;
        }

        if (formData.newPassword !== formData.confirmPassword) {
            alert("New password and confirm password do not match.");
            return;
        }

        try {
            const payload = {
                userId: userData.userId,
                currentPassword: formData.currentPassword,
                newPassword: formData.newPassword
            };

            const { data, status } = await axios.put('http://localhost:8081/user/change/password', payload,
                { headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` } }
            );

            if (status === 200) {
                alert(data.message);
                setFormData({ currentPassword: '', newPassword: '', confirmPassword: '' });
                navigate('/settings/profile');
                console.log('Password updated successfully');
            }
        } catch (err) {
            const message = err.response?.data.error || 'Password update failed';
            alert(message);
            console.error('Password update error:', message);
        }
    };


    const togglePasswordVisibility = (field) => {
        setShowPassword(prev => ({
            ...prev,
            [field]: !prev[field]
        }));
    };

    return (
        <div className="container px-5 py-4">
            <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }} className="mb-3">
                Change Password
            </div>

            <form onSubmit={handleProfileSubmit}>
                <div style={{ height: '300px', overflowY: 'auto' }}>
                    {formFields.map((ref, index) => (
                        <div key={index} className="mb-4">
                            <label className="form-label fw-bold">{ref.label}</label>
                            <div className="position-relative">
                                <input
                                    type={showPassword[ref.name] ? "text" : "password"}
                                    className="form-control pe-5"
                                    id={ref.name}
                                    name={ref.name}
                                    placeholder={ref.placeholder}
                                    value={formData[ref.name]}
                                    onChange={handleChange}
                                    required
                                    readOnly={editField !== ref.name}
                                    onClick={() => handleEditClick(ref.name)}
                                />
                                <span
                                    onClick={() => togglePasswordVisibility(ref.name)}
                                    style={{
                                        position: "absolute",
                                        right: "15px",
                                        top: "45%",
                                        transform: "translateY(-50%)",
                                        cursor: "pointer",
                                        color: "#6c757d"
                                    }}
                                >
                                    {showPassword[ref.name] ? <FaEyeSlash /> : <FaEye />}
                                </span>
                            </div>
                        </div>
                    ))}
                </div>

                <div className='text-end my-5'>
                    <button type="submit" className="btn btn-dark">Update Password</button>
                </div>
            </form>
        </div>
    );
};
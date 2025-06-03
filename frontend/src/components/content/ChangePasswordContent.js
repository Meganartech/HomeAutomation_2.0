import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import axios from 'axios';

export default function ChangePasswordContent() {
    const [formData, setFormData] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
    const [showPassword, setShowPassword] = useState({ currentPassword: false, newPassword: false, confirmPassword: false });
    const navigate = useNavigate();

    const formField = [
        { name: 'currentPassword', placeholder: 'Current Password', label: 'Current Password' },
        { name: 'newPassword', placeholder: 'New Password', label: 'New Password' },
        { name: 'confirmPassword', placeholder: 'Confirm Password', label: 'Confirm Password' }
    ];

    const userData = useSelector((state) => state.user);

    const token = localStorage.getItem('token');

    useEffect(() => {
        if (!token) {
            navigate('/');
            return;
        }
    }, [navigate, token]);

    const resetForm = () => {
        setFormData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    };

    const toggle = (field) => {
        setShowPassword({ ...formData, [field]: !formData[field] });
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!token) {
            navigate('/');
            return;
        }
        if (formData.newPassword !== formData.confirmPassword) {
            alert('New password and confirm password do not match.');
            return;
        }
        try {
            const payload = { userId: userData.userId, currentPassword: formData.currentPassword, newPassword: formData.newPassword };

            const { data, status } = await axios.patch('http://localhost:8081/user/change/password', payload,
                { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                alert(data.message);
                navigate('/settings/profile');
            }
        } catch (err) {
            const error = err.response?.data.error || 'Password update failed';
            alert(error);
            console.error('Error:', error);
        } finally {
            resetForm();
        }
    };

    return (
        <>
            <div className='container px-5 py-4'>

                <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }} className='mb-3'>Change Password</div>

                <form onSubmit={handleSubmit}>
                    <div style={{ height: '300px', overflowY: 'auto' }}>
                        {formField.map((formFieldObj, index) => (
                            <React.Fragment key={index}>
                                <div className='position-relative mb-3'>
                                    <label className='form-label fw-bold'>{formFieldObj.label}</label>
                                    <input type={showPassword[formFieldObj.name] ? 'text' : 'password'} className='form-control pe-5' id={formFieldObj.name} name={formFieldObj.name} placeholder={formFieldObj.placeholder} value={formData[formFieldObj.name]} onChange={handleChange} required />
                                    <span onClick={() => toggle(formFieldObj.name)} style={{ position: 'absolute', right: '15px', top: '70%', transform: 'translateY(-50%)', cursor: 'pointer', color: '#6c757d' }}>
                                        {showPassword[formFieldObj.name] ? <FaEyeSlash /> : <FaEye />}
                                    </span>
                                </div>
                            </React.Fragment>
                        ))}
                    </div>

                    <div className='text-end my-5'>
                        <button type='submit' className='btn btn-dark'>Update Password</button>
                    </div>
                </form>
            </div>
        </>
    );
};
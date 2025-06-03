import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import AuthBackground from '../components/layout/AuthLayout';

const INITIAL_STATE = { name: '', email: '', mobileNumber: '', password: '', confirmPassword: '' };

export default function Register() {

    const [formData, setFormData] = useState(INITIAL_STATE);
    const [showPassword, setShowPassword] = useState({ password: false, confirmPassword: false });
    const navigate = useNavigate();

    const formField = [
        { name: 'name', type: 'text', label: 'Name', autoComplete: 'name' },
        { name: 'email', type: 'email', label: 'Email', autoComplete: 'email' },
        { name: 'mobileNumber', type: 'tel', label: 'Contact No', autoComplete: 'tel' },
        { name: 'password', label: 'Password', autoComplete: 'new-password' },
        { name: 'confirmPassword', label: 'Confirm Password', autoComplete: 'new-password' },
    ];

    const resetForm = () => setFormData(INITIAL_STATE);

    const toggle = (field) => {
        setShowPassword({ ...showPassword, [field]: !showPassword[field] });
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (formData.password !== formData.confirmPassword) {
            alert('Passwords do not match!');
            return;
        }
        try {
            const { data, status } = await axios.post('http://localhost:8081/user/register', formData);
            if (status === 200) {
                alert(data.message);
                navigate('/');
            }
        } catch (err) {
            const error = err.data?.error || 'Register Failed';
            console.error(error);
        } finally {
            resetForm();
        }
    };

    const authBackgroundRegister = { maxWidth: '500px', width: '100%' };
    const passwordIconStyle = { position: 'absolute', right: '15px', top: '65%', transform: 'translateY(-50%)', cursor: 'pointer', color: '#6c757d' };

    return (
        <>
            <AuthBackground customStyle={authBackgroundRegister}>

                <h3 className='mb-3'>Register</h3>

                <form onSubmit={handleSubmit}>
                    {formField.map((formFieldObj, index) => (
                        <React.Fragment key={index}>
                            <div className='position-relative mb-3'>
                                <label className='text-6c757d' htmlFor={formFieldObj.name}>{formFieldObj.label}</label>
                                <input className='form-control' type={(formFieldObj.name === 'password' || formFieldObj.name === 'confirmPassword') ? (showPassword[formFieldObj.name] ? 'text' : 'password') : formFieldObj.type} id={formFieldObj.name} name={formFieldObj.name} value={formData[formFieldObj.name]} onChange={handleChange} autoComplete={formFieldObj.autoComplete} required />
                                {(formFieldObj.name === 'password' || formFieldObj.name === 'confirmPassword') && (
                                    <span onClick={() => toggle(formFieldObj.name)} style={passwordIconStyle}>
                                        {showPassword[formFieldObj.name] ? <FaEyeSlash /> : <FaEye />}
                                    </span>
                                )}
                            </div>
                        </React.Fragment>
                    ))}

                    <div className='mb-3'>
                        <button type='submit' className='btn btn-dark w-100'>Register</button>
                    </div>
                </form>

                <p className='text-center text-muted mb-3'>
                    By registering you agree to <strong>Terms & Conditions</strong> and <strong>Privacy Policy</strong> of the Vermo
                </p>

            </AuthBackground>
        </>
    );
};
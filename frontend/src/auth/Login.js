import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useDispatch } from 'react-redux';
import { setUserData } from '../redux/slice';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import AuthBackground from '../components/layout/AuthLayout';

const INITIAL_STATE = { email: '', password: '' };

export default function Login() {

    const [formData, setFormData] = useState(INITIAL_STATE);
    const [showPassword, setShowPassword] = useState(false);
    const navigate = useNavigate();

    const dispatch = useDispatch();

    const formField = [
        { name: 'email', type: 'email', label: 'Email', autoComplete: 'email' },
        { name: 'password', type: showPassword ? 'text' : 'password', label: 'Password', autoComplete: 'new-password' }
    ];

    const resetForm = () => setFormData(INITIAL_STATE);

    const toggle = () => setShowPassword(!showPassword);

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
                localStorage.setItem('name', data.name);
                localStorage.setItem('userId', data.userId);
                dispatch(setUserData({ userId: data.userId, fullName: data.name }));
                alert('Login successful!');
                navigate('/home');
            }
        } catch (err) {
            const error = err.data?.error || 'Login Failed';
            console.error(error);
        } finally {
            resetForm();
        }
    };

    const authBackgroundLogin = { width: "100%", maxWidth: "450px", minHeight: "450px" };
    const passwordIconStyle = { position: 'absolute', right: '15px', top: '65%', transform: 'translateY(-50%)', cursor: 'pointer', color: '#6c757d' };

    return (
        <>
            <AuthBackground customStyle={authBackgroundLogin}>

                <h3 className='mb-3'>Login</h3>

                <p className='text-muted mb-3'>Please fill your detail to access your account.</p>

                <form onSubmit={handleSubmit}>
                    {formField.map((formFieldObj, index) => (
                        <React.Fragment key={index}>
                            <div className='position-relative mb-3'>
                                <label className='text-6c757d' htmlFor={formFieldObj.name}>{formFieldObj.label}</label>
                                <input className='form-control' type={formFieldObj.type} id={formFieldObj.name} name={formFieldObj.name} value={formData[formFieldObj.name]} onChange={handleChange} autoComplete={formFieldObj.autoComplete} required />
                                {formFieldObj.name === 'password' && (
                                    <span onClick={toggle} style={passwordIconStyle}>
                                        {showPassword ? <FaEyeSlash /> : <FaEye />}
                                    </span>
                                )}
                            </div>
                        </React.Fragment>
                    ))}

                    <div className='text-end mb-3'>
                        <Link className='text-danger' to={'/forgot/password'}>Forgot Password?</Link>
                    </div>

                    <div className='mb-3'>
                        <button type='submit' className='btn btn-dark w-100'>Sign in</button>
                    </div>
                </form>

                <p className='text-center text-muted mb-3'>
                    Don't have an account?{' '}
                    <Link className='btn btn-link p-0 border-0 mb-1 text-dark' to={'/register'}>Register</Link>
                </p>

            </AuthBackground>
        </>
    );
};
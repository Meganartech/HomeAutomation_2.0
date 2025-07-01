import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import axios from 'axios';
import ModalLayout from '../components/layout/ModalLayout';
import AuthLayout from '../components/layout/AuthLayout';

const INITIAL_STATE = { name: '', email: '', contactNumber: '', password: '', confirmPassword: '' };
const authBackgroundStyle = { maxWidth: '500px', width: '100%' };
const passwordIconStyle = { position: 'absolute', right: '15px', top: '65%', transform: 'translateY(-50%)', cursor: 'pointer', color: '#6c757d' };

export default function Register() {
    const [formData, setFormData] = useState(INITIAL_STATE);
    const [showPassword, setShowPassword] = useState({ password: false, confirmPassword: false });
    const [modal, setModal] = useState({ show: false, title: '', message: '', isError: false, onConfirm: null });
    const navigate = useNavigate();

    const formField = [
        { name: 'name', type: 'text', label: 'Name', autoComplete: 'name' },
        { name: 'email', type: 'email', label: 'Email', autoComplete: 'email' },
        { name: 'contactNumber', type: 'tel', label: 'Contact No', autoComplete: 'tel' },
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
            setModal({
                show: true,
                title: 'Error',
                message: <span className='text-danger'>Passwords do not match</span>,
                isError: true,
                onConfirm: () => setModal({ ...modal, show: false }),
            });
            return;
        }
        try {
            const { data, status } = await axios.post(`${process.env.REACT_APP_API_URL}/user/register`, formData);
            if (status === 200) {
                setModal({
                    show: true,
                    title: 'Success',
                    message: data.message,
                    isError: false,
                    onConfirm: () => {
                        setModal({ ...modal, show: false });
                        navigate('/');
                    }
                });
                resetForm();
            }
        } catch (err) {
            const errorMessage = err.response.data?.error || 'Registration failed. Please try again.';
            setModal({
                show: true,
                title: 'Failed',
                message: <span className='text-danger'>{errorMessage}</span>,
                isError: true,
                onConfirm: () => setModal({ ...modal, show: false }),
            });
        }
    };

    return (
        <>
            <AuthLayout customStyle={authBackgroundStyle}>

                <h3 className='mb-3'>Register</h3>

                {/* Form */}
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

            </AuthLayout>

            {/* Alert Modal */}
            {modal.show && (
                <ModalLayout title={modal.title} msg={modal.message} modal={modal.onConfirm} hideClose={!modal.isError}>
                    <button onClick={modal.onConfirm} className={`btn btn-dark px-3`}>
                        {modal.isError ? 'Try Again' : 'OK'}
                    </button>
                </ModalLayout>
            )}
        </>
    );
};
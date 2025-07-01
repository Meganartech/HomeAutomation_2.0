import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import axios from 'axios';
import ModalLayout from '../components/layout/ModalLayout';
import AuthLayout from '../components/layout/AuthLayout';

const INITIAL_STATE = { newPassword: '', confirmPassword: '' };
const authBackgroundStyle = { width: '100%', maxWidth: '450px', minHeight: '450px' };
const passwordIconStyle = { position: 'absolute', right: '15px', top: '65%', transform: 'translateY(-50%)', cursor: 'pointer', color: '#6c757d' };

export default function ResetPassword() {
    const [formData, setFormData] = useState(INITIAL_STATE);
    const [showPassword, setShowPassword] = useState(false);
    const [modal, setModal] = useState({ show: false, title: '', message: '', isError: false, onConfirm: null });
    const navigate = useNavigate();
    const location = useLocation();
    const { email } = location.state || {};

    const formField = [
        { name: 'newPassword', label: 'New Password', autoComplete: 'new-password' },
        { name: 'confirmPassword', label: 'Confirm Password', autoComplete: 'new-password' }
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
        if (formData.newPassword !== formData.confirmPassword) {
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
            const { data, status } = await axios.patch(`${process.env.REACT_APP_API_URL}/user/reset/password`,
                { email, newPassword: formData.newPassword }
            );
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
            const errorMessage = err.response.data?.error || 'Reset Password failed. Please try again.';
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

                <h3 className='mb-3'>Reset Password</h3>

                <p className='text-muted mb-3'>Please enter your password and confirm the password.</p>

                {/* Form */}
                <form onSubmit={handleSubmit}>
                    {formField.map((formFieldObj, index) => (
                        <React.Fragment key={index}>
                            <div className='position-relative mb-3'>
                                <label className='text-6c757d' htmlFor={formFieldObj.name}>{formFieldObj.label}</label>
                                <input className='form-control' type={(formFieldObj.name === 'newPassword' || formFieldObj.name === 'confirmPassword') ? (showPassword[formFieldObj.name] ? 'text' : 'password') : formFieldObj.type} id={formFieldObj.name} name={formFieldObj.name} value={formData[formFieldObj.name]} onChange={handleChange} autoComplete={formFieldObj.autoComplete} required />
                                {(formFieldObj.name === 'newPassword' || formFieldObj.name === 'confirmPassword') && (
                                    <span onClick={() => toggle(formFieldObj.name)} style={passwordIconStyle}>
                                        {showPassword[formFieldObj.name] ? <FaEyeSlash /> : <FaEye />}
                                    </span>
                                )}
                            </div>
                        </React.Fragment>
                    ))}
                    <div className='mb-3'>
                        <button type='submit' className='btn btn-dark w-100'>Save</button>
                    </div>
                </form>

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
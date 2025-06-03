import React, { useEffect, useState, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { FaEdit } from 'react-icons/fa';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import axios from 'axios';
import ModalLayout from '../layout/ModalLayout';

const passwordIconStyle = { position: 'absolute', right: '40px', top: '125px', transform: 'translateY(-50%)', cursor: 'pointer', color: '#6c757d' };
const otpInputStyle = { width: '50px', fontSize: '24px', borderBottom: '', borderRadius: '0', background: 'transparent' };

const Buttons = ({ onCancel }) => (
    <div className='d-flex justify-content-around'>
        <button type='button' className='btn btn-outline-eaeaea px-5' onClick={onCancel}>Cancel</button>
        <button type='submit' className='btn btn-dark px-5'>Verify</button>
    </div>
);

export default function ProfileContent() {
    const [formData, setFormData] = useState({ name: '', email: '', mobileNumber: '' });
    const [showPassword, setShowPassword] = useState(false);
    const [showPasswordModal, setShowPasswordModal] = useState(false);
    const [showOtpVerifyModal, setShowOtpVerifyModal] = useState(false);
    const [editField, setEditField] = useState(null);
    const [passwordField, setPasswordField] = useState('');
    const [otpField, setOtpField] = useState(['', '', '', '', '', '']);
    const [pendingField, setPendingField] = useState(null);
    const inputs = useRef([]);
    const navigate = useNavigate();

    const formField = [
        { name: 'name', id: 'name', type: 'text', label: 'Name' },
        { name: 'mobileNumber', id: 'contact', type: 'tel', label: 'Mobile Number' },
        { name: 'email', id: 'email', type: 'email', label: 'Email' }
    ];

    const token = localStorage.getItem('token');

    useEffect(() => {
        const fetchProfile = async () => {
            if (!token) {
                navigate('/');
                return;
            }
            try {
                const { data, status } = await axios.get('http://localhost:8081/user/profile',
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                if (status === 200) {
                    setFormData({ name: data.name || '', email: data.email || '', mobileNumber: data.mobileNumber || '' });
                }
            } catch (err) {
                const error = err.response?.data?.error || 'Profile fetch failed';
                console.error('Error:', error);
            }
        };
        fetchProfile();
    }, [navigate, token]);

    const toggle = () => setShowPassword(!showPassword);

    const handleChange_ProfileUpdate = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit_ProfileUpdate = async (e) => {
        e.preventDefault();
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const { data, status } = await axios.patch('http://localhost:8081/user/profile/update', formData,
                { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                alert(data.message);
                setEditField(null);
            }
        } catch (err) {
            const error = err.response?.data?.error || 'Profile update failed';
            alert(error);
            console.error('Error:', error);
        }
    };

    const handleClick_Edit = (field) => {
        if (field === 'email' || field === 'mobileNumber') {
            setPendingField(field);
            setShowPasswordModal(true);
        } else {
            setEditField(field);
        }
    };

    const handleSubmit_PasswordVerify = async (e) => {
        e.preventDefault();
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const { data, status } = await axios.post('http://localhost:8081/user/password/verify', { password: passwordField },
                { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                alert(data.message);
                setPendingField(pendingField);
                setPasswordField('');
                setShowPasswordModal(false);
                setShowOtpVerifyModal(true);
                setEditField(null);
            }
        } catch (err) {
            setPasswordField('');
            const error = err.response?.data?.error || 'Password verification failed';
            alert(error);
            console.error('Error:', error);
        }
    };

    const handleKeyDown = (index, e) => {
        if (e.key === 'Backspace' && !otpField[index] && index > 0) {
            inputs.current[index - 1].focus();
        }
    };

    const handleChange_OTP = (index, value) => {
        if (!/^\d?$/.test(value)) return;
        const newOtp = [...otpField];
        newOtp[index] = value;
        setOtpField(newOtp);
        if (value && index < 5) {
            inputs.current[index + 1].focus();
        }
    };

    const handleSubmit_OTP = async (e) => {
        e.preventDefault();
        const otp = otpField.join('');
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const { data, status } = await axios.post('http://localhost:8081/user/otp/verify', { email: formData.email, otp },
                { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                alert(data.message);
                setEditField(pendingField);
                setShowOtpVerifyModal(false);
                setOtpField(['', '', '', '', '', '']);
                setPendingField(null);
            }
        } catch (err) {
            const error = err.response?.data?.error || 'OTP verification failed';
            alert(error);
            console.error('Error:', error);
        }
    };

    return (
        <>
            <div className='container px-5 py-4'>

                <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }} className='mb-3'>Profile</div>

                <form onSubmit={handleSubmit_ProfileUpdate}>
                    <div style={{ height: '300px', overflowY: 'auto' }}>
                        {formField.map((formFieldObj, index) => (
                            <React.Fragment key={index}>
                                <div className='position-relative mb-3'>
                                    <label htmlFor={formFieldObj.id} className='form-label fw-bold'>{formFieldObj.label}</label>
                                    <input type={formFieldObj.type} className='form-control' id={formFieldObj.id} name={formFieldObj.name} placeholder={formFieldObj.label} value={formData[formFieldObj.name]} onChange={handleChange_ProfileUpdate} required readOnly={editField !== formFieldObj.name} />
                                    <span style={{ position: 'absolute', right: '15px', top: '70%', transform: 'translateY(-50%)', cursor: 'pointer', color: editField === formFieldObj.name ? '#1f1f1f' : '#6c757d' }} onClick={() => handleClick_Edit(formFieldObj.name)}>
                                        <FaEdit />
                                    </span>
                                </div>
                            </React.Fragment>
                        ))}
                        <div className='text-end mb-3'>
                            <Link to={'/settings/change_password'} className='text-decoration-none text-dark'>
                                Want to Change Your Password ?
                            </Link>
                        </div>
                    </div>
                    <div className='text-end my-5'>
                        {editField && (
                            <>
                                <button type='button' className='btn btn-outline-eaeaea me-3' onClick={() => setEditField(null)}>Cancel</button>
                                <button type='submit' className='btn btn-dark'>Submit</button>
                            </>
                        )}
                    </div>
                </form>

            </div>

            {/* Password Modal */}
            {showPasswordModal && (
                <ModalLayout title={'Verify Password'} modal={setShowPasswordModal}>
                    <form onSubmit={handleSubmit_PasswordVerify}>
                        <div className='text-start mb-5'>
                            <label className='form-label'>Password</label>
                            <input type={showPassword ? 'text' : 'password'} className='form-control' value={passwordField} onChange={(e) => setPasswordField(e.target.value)} required autoFocus />
                            <span onClick={toggle} style={passwordIconStyle}>{showPassword ? <FaEyeSlash /> : <FaEye />}</span>
                        </div>
                        <Buttons onCancel={() => { setShowPasswordModal(false); setPasswordField(''); setPendingField(null); }} />
                    </form>
                </ModalLayout>
            )}

            {/* OTP Modal */}
            {showOtpVerifyModal && (
                <ModalLayout title={'Verify OTP'} modal={setShowOtpVerifyModal}>
                    <form onSubmit={handleSubmit_OTP}>
                        <div className='text-start mb-5'>
                            <label className='form-label'>OTP</label>
                            <div className='d-flex justify-content-around mb-3'>
                                {otpField.map((otpFieldObj, index) => (
                                    <input key={index} ref={el => inputs.current[index] = el} type='text' maxLength='1' value={otpFieldObj} onChange={(e) => handleChange_OTP(index, e.target.value)} onKeyDown={(e) => handleKeyDown(index, e)} className='form-control rounded text-center mx-1' style={otpInputStyle} required />
                                ))}
                            </div>
                        </div>
                        <Buttons onCancel={() => { setShowOtpVerifyModal(false); setOtpField(''); setPendingField(null); }} />
                    </form>
                </ModalLayout>
            )}
        </>
    );
};
import React, { useEffect, useState, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { FaEdit } from 'react-icons/fa';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import { useDispatch } from 'react-redux';
import { setUserData } from '../../redux/slice';
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
    const [formData, setFormData] = useState({ name: '', email: '', contactNumber: '' });
    const [showPassword, setShowPassword] = useState(false);
    const [showPasswordModal, setShowPasswordModal] = useState(false);
    const [showOtpVerifyModal, setShowOtpVerifyModal] = useState(false);
    const [editField, setEditField] = useState(null);
    const [passwordField, setPasswordField] = useState('');
    const [otpField, setOtpField] = useState(['', '', '', '', '', '']);
    const [pendingField, setPendingField] = useState(null);
    const inputs = useRef([]);
    const [modal, setModal] = useState({ show: false, title: '', message: '', isError: false, onConfirm: null });
    const navigate = useNavigate();

    const dispatch = useDispatch();

    const formField = [
        { name: 'name', id: 'name', type: 'text', label: 'Name', autoComplete: 'name' },
        { name: 'contactNumber', id: 'contact', type: 'tel', label: 'Contact Number', autoComplete: 'tel' },
        { name: 'email', id: 'email', type: 'email', label: 'Email', autoComplete: 'email' }
    ];

    const token = localStorage.getItem('token');

    useEffect(() => {
        const fetchProfile = async () => {
            if (!token) {
                navigate('/');
                return;
            }
            try {
                const { data, status } = await axios.get(`${process.env.REACT_APP_API_URL}/user/profile`,
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                if (status === 200) {
                    setFormData({ name: data.name || '', email: data.email || '', contactNumber: data.contactNumber || '' });
                }
            } catch (err) {
                const errorMessage = err.response?.data?.error || 'Profile fetch failed';
                console.error(errorMessage);
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
            const { data, status } = await axios.patch(`${process.env.REACT_APP_API_URL}/user/profile/update`, formData,
                { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                setModal({
                    show: true,
                    title: 'Success',
                    message: data.message,
                    isError: false,
                    onConfirm: () => {
                        setModal({ ...modal, show: false });
                        setEditField(null);
                    }
                });
                dispatch(setUserData({ fullName: formData.name }));
            }
        } catch (err) {
            const errorMessage = err.response?.data?.error || 'Profile update failed. Please try again.';
            setModal({
                show: true,
                title: 'Failed',
                message: <span className='text-danger'>{errorMessage}</span>,
                isError: true,
                onConfirm: () => setModal({ ...modal, show: false }),
            });
        }
    };

    const handleClick_Edit = (field) => {
        if (field === 'email' || field === 'contactNumber') {
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
                setModal({
                    show: true,
                    title: 'Success',
                    message: data.message,
                    isError: false,
                    onConfirm: () => {
                        setModal({ ...modal, show: false });
                        setPendingField(pendingField);
                        setPasswordField('');
                        setShowPasswordModal(false);
                        setShowOtpVerifyModal(true);
                        setEditField(null);
                    }
                });
            }
        } catch (err) {
            setPasswordField('');
            const errorMessage = err.response?.data?.error || 'Password verification failed. Please try again.';
            setModal({
                show: true,
                title: 'Failed',
                message: <span className='text-danger'>{errorMessage}</span>,
                isError: true,
                onConfirm: () => setModal({ ...modal, show: false }),
            });
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
            const { data, status } = await axios.post(`${process.env.REACT_APP_API_URL}/user/otp/verify`, { email: formData.email, otp },
                { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                setModal({
                    show: true,
                    title: 'Success',
                    message: data.message,
                    isError: false,
                    onConfirm: () => {
                        setModal({ ...modal, show: false });
                        setEditField(pendingField);
                        setShowOtpVerifyModal(false);
                        setOtpField(['', '', '', '', '', '']);
                        setPendingField(null);
                    }
                });
            }
        } catch (err) {
            const errorMessage = err.response?.data?.error || 'OTP verification failed. Please try again.';
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
            <div className='container px-5 py-4'>

                <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }} className='mb-3'>Profile</div>

                {/* Form */}
                <form onSubmit={handleSubmit_ProfileUpdate}>

                    <div style={{ width: '100%', overflowX: 'hidden', height: '300px', overflowY: 'auto' }}>
                        {formField.map((formFieldObj, index) => (
                            <React.Fragment key={index}>
                                <div className='position-relative mb-3'>
                                    <label htmlFor={formFieldObj.id} className='form-label fw-bold'>{formFieldObj.label}</label>
                                    <input type={formFieldObj.type} className='form-control' id={formFieldObj.id} name={formFieldObj.name} placeholder={formFieldObj.label} value={formData[formFieldObj.name]} onChange={handleChange_ProfileUpdate} autoComplete={formFieldObj.autoComplete} required readOnly={editField !== formFieldObj.name} />
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
                <ModalLayout title={'Verify Password'} modal={() => setShowPasswordModal(false)}>
                    <form onSubmit={handleSubmit_PasswordVerify}>
                        <div className='text-start mb-5'>
                            <label className='form-label' htmlFor='password'>Password</label>
                            <input type={showPassword ? 'text' : 'password'} id='password' className='form-control' value={passwordField} onChange={(e) => setPasswordField(e.target.value)} required autoFocus />
                            <span onClick={toggle} style={passwordIconStyle}>{showPassword ? <FaEyeSlash /> : <FaEye />}</span>
                        </div>
                        <Buttons onCancel={() => { setShowPasswordModal(false); setPasswordField(''); setPendingField(null); }} />
                    </form>
                </ModalLayout>
            )}

            {/* OTP Modal */}
            {showOtpVerifyModal && (
                <ModalLayout title={'Verify OTP'} modal={() => setShowOtpVerifyModal(false)}>
                    <form onSubmit={handleSubmit_OTP}>
                        <div className='text-start mb-5'>
                            <label className='form-label' htmlFor='otp'>OTP</label>
                            <div className='d-flex justify-content-around mb-3'>
                                {otpField.map((otpFieldObj, index) => (
                                    <input key={index} ref={el => inputs.current[index] = el} type='text' id='otp' maxLength='1' value={otpFieldObj} onChange={(e) => handleChange_OTP(index, e.target.value)} onKeyDown={(e) => handleKeyDown(index, e)} className='form-control rounded text-center mx-1' style={otpInputStyle} required />
                                ))}
                            </div>
                        </div>
                        <Buttons onCancel={() => { setShowOtpVerifyModal(false); setOtpField(''); setPendingField(null); }} />
                    </form>
                </ModalLayout>
            )}

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
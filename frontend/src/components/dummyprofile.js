import React, { useEffect, useState, useRef } from 'react';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { setUserData } from '../redux/slice';
import { FaEdit } from "react-icons/fa";
import { FaEye, FaEyeSlash } from "react-icons/fa";

export default function ProfileContent() {
    const [formData, setFormData] = useState({ name: '', email: '', mobileNumber: '' });
    const [editField, setEditField] = useState(null);
    const [showPasswordModal, setShowPasswordModal] = useState(false);
    const [showOtpVerifyModal, setShowOtpVerifyModal] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [passwordField, setPasswordField] = useState('');
    const [otpField, setOtpField] = useState(["", "", "", "", "", ""]);
    const [pendingField, setPendingField] = useState(null);
    const inputs = useRef([]);
    const navigate = useNavigate();

    const dispatch = useDispatch();

    useEffect(() => {
        const fetchProfile = async () => {
            const token = localStorage.getItem('token');
            if (!token) {
                navigate('/user/login');
                return;
            }
            try {
                const { data, status } = await axios.get("http://localhost:8081/user/profile",
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                if (status === 200) {
                    setFormData({
                        name: data.name || "",
                        email: data.email || "",
                        mobileNumber: data.mobileNumber || "",
                    });
                    dispatch(setUserData({ userId: data.userId, fullName: data.name }));
                    console.log('Profile fetched successfully');
                }
            } catch (err) {
                const message = err.response?.data?.error || 'Profile fetching failed';
                alert(message);
                console.log('Profile error:', message);
            }
        };
        fetchProfile();
    }, [navigate, dispatch]);

    const handleProfileChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleProfileSubmit = async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/user/login');
            return;
        }
        try {
            const { data, status } = await axios.put('http://localhost:8081/user/profile/update', formData,
                { headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                alert(data.message);
                dispatch(setUserData({ fullName: formData.name }));
                console.log('Profile updated successfully');
                setEditField(null);
            }
        } catch (err) {
            const message = err.response?.data?.error || 'Profile update failed';
            alert(message);
            console.error('Profile update error:', message);
        }
    };

    const handleEditClick = (field) => {
        if (field === 'email' || field === 'mobileNumber') {
            setPendingField(field);
            setShowPasswordModal(true);
        } else {
            setEditField(field);
        }
    };

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    const handlePasswordVerifySubmit = async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/user/login');
            return;
        }
        try {
            const { data, status } = await axios.post('http://localhost:8081/user/password/verify', { password: passwordField },
                { headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                alert(data.message);
                setPendingField(pendingField);
                setShowPasswordModal(false);
                setPasswordField('');
                setShowOtpVerifyModal(true);
                setEditField(null);
            } else {
                alert(data.error || 'Password verification failed');
            }
        } catch (err) {
            setPasswordField('');
            const message = err.response?.data?.error || 'Password verification failed';
            alert(message);
            console.error('Password verification error:', message);
        }
    };

    const handleOtpVerifyChange = (index, value) => {
        if (!/^\d?$/.test(value)) return;
        const newOtp = [...otpField];
        newOtp[index] = value;
        setOtpField(newOtp);
        if (value && index < 5) {
            inputs.current[index + 1].focus();
        }
    };

    const handleKeyDown = (index, e) => {
        if (e.key === "Backspace" && !otpField[index] && index > 0) {
            inputs.current[index - 1].focus();
        }
    };

    const handleOtpVerifySubmit = async (e) => {
        e.preventDefault();
        const otp = otpField.join("");

        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/user/login');
            return;
        }

        try {
            const { data, status } = await axios.post("http://localhost:8081/user/otp/verify", { email: formData.email, otp },
                { headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                alert(data.message);
                setEditField(pendingField);
                setShowOtpVerifyModal(false);
                setOtpField(["", "", "", "", "", ""]);
                setPendingField(null);
            }
        } catch (err) {
            if (err.response?.data?.error) {
                alert(`${err.response.data.error}`);
                console.log(err.response.data.error);
            } else {
                alert('OTP verification failed due to server error.');
            }
        }
    };

    return (
        <>
            <div className="container px-5 py-4">
                <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }} className="mb-3">Profile</div>

                <form onSubmit={handleProfileSubmit}>

                    <div style={{ height: '300px', overflowY: 'auto' }}>
                        <div className="position-relative mb-3">
                            <label htmlFor="name" className="form-label fw-bold">Name</label>
                            <input type="text" className="form-control" id="name" name='name' placeholder="Name" value={formData.name} onChange={handleProfileChange} required readOnly={editField !== 'name'} />
                            <span style={{ position: "absolute", right: "15px", top: "70%", transform: "translateY(-50%)", cursor: "pointer", color: editField === 'name' ? "#0d6efd" : "#6c757d" }}
                                onClick={() => handleEditClick('name')}>{<FaEdit />}</span>
                        </div>

                        <div className="position-relative mb-3">
                            <label htmlFor="contact" className="form-label fw-bold">Contact Number</label>
                            <input type="tel" className="form-control" id="contact" name='mobileNumber' placeholder="Contact Number" value={formData.mobileNumber} onChange={handleProfileChange} required readOnly={editField !== 'mobileNumber'} />
                            <span style={{ position: "absolute", right: "15px", top: "70%", transform: "translateY(-50%)", cursor: "pointer", color: editField === 'mobileNumber' ? "#0d6efd" : "#6c757d" }}
                                onClick={() => handleEditClick('mobileNumber')}>{<FaEdit />}</span>
                        </div>

                        <div className="position-relative mb-3">
                            <label htmlFor="email" className="form-label fw-bold">Email</label>
                            <input type="email" className="form-control" id="email" name='email' placeholder="Email" value={formData.email} onChange={handleProfileChange} required readOnly={editField !== 'email'} />
                            <span style={{ position: "absolute", right: "15px", top: "70%", transform: "translateY(-50%)", cursor: "pointer", color: editField === 'email' ? "#0d6efd" : "#6c757d" }}
                                onClick={() => handleEditClick('email')}>{<FaEdit />}</span>
                        </div>

                        {/* <div className="mb-3">
                            <label htmlFor="email" className="form-label fw-bold">Email</label>
                            <input type="email" className="form-control" id="email" placeholder="Email" />
                        </div> */}

                        <div className="text-end mb-3">
                            <Link to={"/settings/change_password_"} className="text-decoration-none text-dark">
                                Want to Change Your Password?
                            </Link>
                        </div>
                    </div>

                    <div className='text-end my-5'>
                        {editField && (
                            <>
                                <button type="button" className="btn btn-outline-secondary me-3" onClick={() => setEditField(null)}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn btn-dark">Submit</button>
                            </>
                        )}
                    </div>

                </form>

            </div>

            {/* Custom Password Modal */}
            {showPasswordModal && (
                <div style={{ position: "fixed", top: 0, left: 0, right: 0, bottom: 0, backgroundColor: "rgba(0,0,0,0.5)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 1050 }}>

                    <div style={{ backgroundColor: "#fff", borderRadius: "20px", width: "100%", maxWidth: "440px", boxShadow: "0 10px 25px rgba(0,0,0,0.2)", padding: "24px", position: "relative", textAlign: "center" }}>

                        <div style={{ fontWeight: 600, fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }} className='mb-3'>Verify Password</div>

                        <form onSubmit={handlePasswordVerifySubmit}>
                            <input type={showPassword ? "text" : "password"} className="form-control mb-5" placeholder="Enter Your Password" value={passwordField} onChange={(e) => setPasswordField(e.target.value)} required autoFocus />
                            <span onClick={togglePasswordVisibility} style={{ position: "absolute", right: "32px", top: "82px", transform: "translateY(-50%)", cursor: "pointer", color: "#6c757d" }}>{showPassword ? <FaEyeSlash /> : <FaEye />}</span>
                            <div className="d-flex justify-content-around">
                                <button type="button" className="btn btn-outline-dark px-5 " onClick={() => { setShowPasswordModal(false); setPasswordField(''); setPendingField(null); }}>Cancel</button>
                                <button type="submit" className="btn btn-dark px-5">Verify</button>
                            </div>
                        </form>

                    </div>

                </div>
            )}

            {showOtpVerifyModal && (
                <div style={{ position: "fixed", top: 0, left: 0, right: 0, bottom: 0, backgroundColor: "rgba(0,0,0,0.5)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 1050 }}>
                    <div style={{ background: "white", padding: "30px", borderRadius: "10px", width: "90%", maxWidth: "400px", boxShadow: "0px 0px 10px rgba(0,0,0,0.25)", position: "relative" }}>
                        <h5 className="mb-3 text-center">Verify OTP</h5>
                        <form onSubmit={handleOtpVerifySubmit}>
                            <div className="d-flex justify-content-between form-floating mb-3">
                                {otpField.map((digit, i) => (
                                    <input
                                        key={i}
                                        ref={el => inputs.current[i] = el}
                                        type="text"
                                        maxLength="1"
                                        value={digit}
                                        onChange={(e) => handleOtpVerifyChange(i, e.target.value)}
                                        onKeyDown={(e) => handleKeyDown(i, e)}
                                        className="form-control text-center mx-1"
                                        style={{ width: "50px", fontSize: "24px", borderBottom: "", borderRadius: "0", background: "transparent" }} required />
                                ))}
                            </div>
                            <div className="d-flex justify-content-end">
                                <button type="submit" className="btn btn-dark">Verify</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </>
    );
};
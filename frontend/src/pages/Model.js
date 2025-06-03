import React, { useEffect, useState, useRef } from 'react';
import axios from 'axios';
import { FaEdit } from "react-icons/fa";
import Sidepanel from '../components/SidePanel';
import Navbar from '../components/Navbar';
import Indicator from '../components/Indicator';
import { useNavigate } from 'react-router-dom';

export default function Profile() {
    const [formData, setFormData] = useState({ name: '', email: '', mobileNumber: '' });
    const [editField, setEditField] = useState(null);
    const [showPasswordModal, setShowPasswordModal] = useState(false);
    const [showOtpVerifyModal, setShowOtpVerifyModal] = useState(false);
    const [passwordField, setPasswordField] = useState('');
    const [otpField, setOtpField] = useState(["", "", "", "", "", ""]);
    const [pendingField, setPendingField] = useState(null);
    const inputs = useRef([]);
    const navigate = useNavigate();

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
                    console.log('Profile fetched successfully');
                }
            } catch (err) {
                const message = err.response?.data?.error || 'Profile fetching failed';
                alert(message);
                console.log('Profile error:', message);
            }
        };
        fetchProfile();
    }, [navigate]);

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

    const handlePasswordVerifySubmit = async () => {
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
                setOtpField(["", "", "", "", "", ""]);
                setEditField(null);
            } else {
                alert(data.error || 'Password verification failed');
            }
        } catch (err) {
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
            <div className="container-fluid position-fixed bg-eaeaea p-0">
                <div className="d-flex">
                    <div className="p-0" style={{ width: '280px' }}>
                        <Sidepanel />
                    </div>
                    <div className="p-3" style={{ width: 'calc(100% - 280px)', height: '100vh', overflowY: 'hidden' }}>
                        <div className='mb-3'>
                            <Navbar />
                        </div>
                        <Indicator />
                        <div className="bg-white p-4 rounded shadow-sm mb-3 position-relative" style={{ height: 'calc(100vh - 210px)', overflowY: 'auto' }}>
                            <div className="container mt-5">
                                <form onSubmit={handleProfileSubmit}>
                                    <div className="form-floating mb-3 position-relative">
                                        <input type="text" name="name" className="form-control mb-1" placeholder='' value={formData.name} onChange={handleProfileChange} required readOnly={editField !== 'name'} />
                                        <label className='text-6c757d'>Name</label>
                                        <span style={{ position: "absolute", right: "15px", top: "60%", transform: "translateY(-50%)", cursor: "pointer", color: editField === 'name' ? "#0d6efd" : "#6c757d" }}
                                            onClick={() => handleEditClick('name')}>{<FaEdit />}</span>
                                    </div>

                                    <div className="form-floating mb-3 position-relative">
                                        <input type="email" name="email" className="form-control mb-1" placeholder='' value={formData.email} onChange={handleProfileChange} required readOnly={editField !== 'email'} />
                                        <label className='text-6c757d'>Email</label>
                                        <span style={{ position: "absolute", right: "15px", top: "60%", transform: "translateY(-50%)", cursor: "pointer", color: editField === 'email' ? "#0d6efd" : "#6c757d" }}
                                            onClick={() => handleEditClick('email')}>{<FaEdit />}</span>
                                    </div>

                                    <div className="form-floating mb-3 position-relative">
                                        <input type="tel" name="mobileNumber" className="form-control mb-1" placeholder='' value={formData.mobileNumber} onChange={handleProfileChange} required readOnly={editField !== 'mobileNumber'} />
                                        <label className='text-6c757d'>Mobile Number</label>
                                        <span style={{ position: "absolute", right: "15px", top: "60%", transform: "translateY(-50%)", cursor: "pointer", color: editField === 'mobileNumber' ? "#0d6efd" : "#6c757d" }}
                                            onClick={() => handleEditClick('mobileNumber')}>{<FaEdit />}</span>
                                    </div>

                                    <div className='text-end mt-4 mb-3'>
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
                        </div>
                    </div>
                </div>
            </div>

            {/* Custom Password Modal */}
            {showPasswordModal && (
                <div style={{ position: "fixed", top: 0, left: 0, right: 0, bottom: 0, backgroundColor: "rgba(0,0,0,0.5)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 1050 }}>
                    <div style={{ background: "white", padding: "30px", borderRadius: "10px", width: "90%", maxWidth: "400px", boxShadow: "0px 0px 10px rgba(0,0,0,0.25)", position: "relative" }}>
                        <h5 className="mb-3 text-center">Verify Password</h5>
                        <input
                            type="password" className="form-control mb-3" placeholder="Enter your password" value={passwordField} onChange={(e) => setPasswordField(e.target.value)} autoFocus />
                        <div className="d-flex justify-content-end">
                            <button type="button" className="btn btn-outline-secondary me-2" onClick={() => { setShowPasswordModal(false); setPasswordField(''); setPendingField(null); }}>Cancel</button>
                            <button type="button" className="btn btn-dark" onClick={handlePasswordVerifySubmit}>Verify</button>
                        </div>
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
                                        style={{ width: "50px", fontSize: "24px", borderBottom: "", borderRadius: "0", background: "transparent" }}
                                        required

                                    />
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


// import { useState } from 'react';

// export default function CreateScenesContent() {
//     const [selectedDays, setSelectedDays] = useState([]);
//     const [selectedAltDays, setSelectedAltDays] = useState([]);
//     const [device, setDevice] = useState('');
//     const [room, setRoom] = useState('');
//     const [condition, setCondition] = useState('On');

//     const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

//     const toggleDay = (day, alt = false) => {
//         const state = alt ? selectedAltDays : selectedDays;
//         const setState = alt ? setSelectedAltDays : setSelectedDays;
//         setState(
//             state.includes(day) ? state.filter(d => d !== day) : [...state, day]
//         );
//     };
//     return (
//         <>
//             <div className='container px-5 py-4'>
//                 <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }} className='mb-3'>Create Scenes</div>

               
//                 <div className="mb-3">
//                     <label className="form-label">Time</label>
//                     <div className="d-flex gap-2">
//                         <input type="time" className="form-control" />
//                         <span className="align-self-center">To</span>
//                         <input type="time" className="form-control" />
//                     </div>
//                 </div>

            
//                 <div className="mb-3">
//                     <label className="form-label">Day</label>
//                     <div className="d-flex flex-wrap gap-2 mb-2">
//                         {days.map(day => (
//                             <button
//                                 key={day}
//                                 className={`btn rounded-circle ${selectedDays.includes(day) ? 'btn-dark text-white' : 'btn-light'
//                                     }`}
//                                 onClick={() => toggleDay(day)}
//                                 style={{ width: '40px', height: '40px' }}
//                             >
//                                 {day}
//                             </button>
//                         ))}
//                         <span className="align-self-center px-2">|</span>
//                         {days.map(day => (
//                             <button
//                                 key={day + 'alt'}
//                                 className={`btn rounded-circle ${selectedAltDays.includes(day)
//                                     ? 'btn-dark text-white'
//                                     : 'btn-light'
//                                     }`}
//                                 onClick={() => toggleDay(day, true)}
//                                 style={{ width: '40px', height: '40px' }}
//                             >
//                                 {day}
//                             </button>
//                         ))}
//                     </div>
//                 </div>

              
//                 <div className="mb-3">
//                     <label className="form-label">Devices</label>
//                     <select
//                         className="form-select"
//                         value={device}
//                         onChange={e => setDevice(e.target.value)}
//                     >
//                         <option value="">Select Device</option>
//                         <option value="Smart Bulb">Smart Bulb</option>
//                     </select>
//                 </div>

         
//                 <div className="mb-3">
//                     <label className="form-label">Rooms</label>
//                     <select
//                         className="form-select"
//                         value={room}
//                         onChange={e => setRoom(e.target.value)}
//                     >
//                         <option value="">Select Room</option>
//                         <option value="Living room">Living room</option>
//                     </select>
//                 </div>

//                 <div className="mb-3">
//                     <label className="form-label">Condition</label>
//                     <div className="btn-group w-100" role="group">
//                         <button
//                             type="button"
//                             className={`btn ${condition === 'On' ? 'btn-dark' : 'btn-light'}`}
//                             onClick={() => setCondition('On')}
//                         >
//                             On
//                         </button>
//                         <button
//                             type="button"
//                             className={`btn ${condition === 'Off' ? 'btn-dark' : 'btn-light'}`}
//                             onClick={() => setCondition('Off')}
//                         >
//                             Off
//                         </button>
//                     </div>
//                 </div>
//             </div>
//         </>
//     );
// };

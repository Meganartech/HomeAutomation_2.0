import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { useDispatch } from 'react-redux';
import { setUserData } from '../redux/slice';
import { FaEdit } from "react-icons/fa";

export default function ProfileContent() {

    const [formData, setFormData] = useState({ name: '', email: '', mobileNumber: '' });
    const [editField, setEditField] = useState(null);
    const navigate = useNavigate();

    const dispatch = useDispatch();

    const token = localStorage.getItem('token');

    useEffect(() => {
        const fetchProfile = async () => {
            if (!token) {
                navigate('/user/login');
                return;
            }

            try {
                const { data, status } = await axios.get("http://localhost:8081/user/profile",
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                if (status === 200) {
                    setFormData({ name: data.name || "", email: data.email || "", mobileNumber: data.mobileNumber || "" });
                    dispatch(setUserData({ userId: data.userId, fullName: data.name }));
                }
            } catch (err) {
                const message = err.data?.error || 'Profile fetching failed';
                alert(message);
                console.log('Profile error:', message);
            } finally {
                resetForm();
            }
        };
        fetchProfile();
    }, [navigate, dispatch, token]);

    const resetForm = () => {
        setFormData({ name: '', email: '', mobileNumber: '' });
    }

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleClick = (field) => {
        if (field === 'email' || field === 'mobileNumber') {
        
        } else {
            setEditField(field);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
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
                setEditField(null);
            }
        } catch (err) {
            const message = err.data?.error || 'Profile update failed';
            alert(message);
            console.error('Profile update error:', message);
        }
    };

    return (
        <>
            <div className="container px-5 py-4">
                <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }} className="mb-3">Profile</div>

                <form onSubmit={handleSubmit}>

                    <div style={{ height: '300px', overflowY: 'auto' }}>
                        <div className="position-relative mb-3">
                            <label htmlFor="name" className="form-label fw-bold">Name</label>
                            <input type="text" className="form-control" id="name" name='name' placeholder="Name" value={formData.name} onChange={handleChange} required readOnly={editField !== 'name'} />
                            <span style={{ position: "absolute", right: "15px", top: "70%", transform: "translateY(-50%)", cursor: "pointer", color: editField === 'name' ? "#0d6efd" : "#6c757d" }}
                                onClick={() => handleClick('name')}>{<FaEdit />}</span>
                        </div>

                        <div className="position-relative mb-3">
                            <label htmlFor="contact" className="form-label fw-bold">Contact Number</label>
                            <input type="tel" className="form-control" id="contact" name='mobileNumber' placeholder="Contact Number" value={formData.mobileNumber} onChange={handleChange} required readOnly={editField !== 'mobileNumber'} />
                            <span style={{ position: "absolute", right: "15px", top: "70%", transform: "translateY(-50%)", cursor: "pointer", color: editField === 'mobileNumber' ? "#0d6efd" : "#6c757d" }}
                                onClick={() => handleClick('mobileNumber')}>{<FaEdit />}</span>
                        </div>

                        <div className="position-relative mb-3">
                            <label htmlFor="email" className="form-label fw-bold">Email</label>
                            <input type="email" className="form-control" id="email" name='email' placeholder="Email" value={formData.email} onChange={handleChange} required readOnly={editField !== 'email'} />
                            <span style={{ position: "absolute", right: "15px", top: "70%", transform: "translateY(-50%)", cursor: "pointer", color: editField === 'email' ? "#0d6efd" : "#6c757d" }}
                                onClick={() => handleClick('email')}>{<FaEdit />}</span>
                        </div>

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
        </>
    );
};
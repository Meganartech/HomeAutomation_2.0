import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function ForgotPassword() {
    const [formData, setFormData] = useState({ email: "" });
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post("http://localhost:8081/user/forgot/password", formData);
            if (response.status === 200) {
                alert("OTP sent to your email. Please check your inbox.");
                navigate('/otp/verify', { state: { email: formData.email } });
            }
        } catch (err) {
            if (err.response?.data?.error) {
                alert(`${err.response.data.error}`);
                console.log(err.response.data.error);
            } else {
                alert('OTP send failed due to server error.');
            }
        };

        setFormData({ email: "" });
    }

    return (
        <>
            <div className="container-fluid d-flex justify-content-center align-items-center bg-eceaea" style={{ minHeight: "100vh" }}>
                <div className="card shadow border-0 p-4 mx-auto" style={{ maxWidth: "450px", width: "100%", minHeight: "450px" }}>
                    <h3 className="mb-3">Forgot Password</h3>
                    <p className="text-muted mb-5">Please enter your registered email to reset your password.</p>

                    <form onSubmit={handleSubmit}>
                        <div className="form-floating mb-3">
                            <input type="email" name="email" className="form-control mb-1" placeholder="" value={formData.email} onChange={handleChange} required />
                            <label className="text-6c757d">Email</label>
                        </div>

                        <div className="mt-10 mb-3">
                            <button type="submit" className="btn btn-dark w-100">Get OTP</button>
                        </div>
                    </form>

                </div>
            </div>
        </>
    );
};
import React, { useState, useRef } from "react";
import axios from "axios";
import { Link, useNavigate, useLocation } from "react-router-dom";

export default function OtpVerify() {
    const [otpData, setOtp] = useState(["", "", "", "", "", ""]);
    const navigate = useNavigate();
    const inputs = useRef([]);
    const location = useLocation();
    const { email } = location.state || {};

    const handleChange = (index, value) => {
        if (!/^\d?$/.test(value)) return;
        const newOtp = [...otpData];
        newOtp[index] = value;
        setOtp(newOtp);

        if (value && index < 5) {
            inputs.current[index + 1].focus();
        }
    };

    const handleKeyDown = (index, e) => {
        if (e.key === "Backspace" && !otpData[index] && index > 0) {
            inputs.current[index - 1].focus();
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const otp = otpData.join("");
        try {
            const response = await axios.post("http://localhost:8081/user/otp/verify", {
                email, otp
            });


            if (response.status === 200) {
                alert("OTP verified successfully. You can now reset your password.");
                navigate('/reset/password', { state: { email } });
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
            <div className="container-fluid d-flex justify-content-center align-items-center bg-eceaea" style={{ minHeight: "100vh" }}>
                <div className="card shadow border-0 p-4 mx-auto" style={{ maxWidth: "450px", width: "100%", minHeight: "450px" }}>
                    <h3 className="mb-3">Verify OTP</h3>
                    <p className="text-muted mb-5">Enter 4 digit code sent to you at your email.</p>

                    <form onSubmit={handleSubmit}>
                        <div className="d-flex justify-content-between form-floating mb-3">
                            {otpData.map((digit, i) => (
                                <input
                                    key={i}
                                    ref={el => inputs.current[i] = el}
                                    type="text"
                                    maxLength="1"
                                    value={digit}
                                    onChange={(e) => handleChange(i, e.target.value)}
                                    onKeyDown={(e) => handleKeyDown(i, e)}
                                    className="form-control text-center mx-1"
                                    style={{ width: "50px", fontSize: "24px", borderBottom: "", borderRadius: "0", background: "transparent" }}
                                    required
                                />
                            ))}
                        </div>

                        <div className="mt-13 mb-3">
                            <button type="submit" className="btn btn-dark w-100">Verify OTP</button>
                        </div>

                        <p className="text-center text-muted m-0">
                            Didn’t receive a verification code?
                        </p>
                        <div className="text-center m-0">
                            <Link className="btn btn-link p-0 text-dark">Resend Code</Link> |
                            <Link className="btn btn-link p-0 text-dark ms-1" to={"/forgot/password"}>Change Email</Link>
                        </div>
                    </form>
                </div>
            </div>
        </>
    );
};
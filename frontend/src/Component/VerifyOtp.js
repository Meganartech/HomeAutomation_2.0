import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import LoggedOutNavbar from "../Component/LoggedOutNavbar";
import Swal from "sweetalert2";

export default function VerifyOtp() {
    const [otp, setOtp] = useState("");
    const [error, setError] = useState("");
    const navigate = useNavigate();
    const location = useLocation();
    const { email } = location.state || {};

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await axios.post("http://localhost:8081/user/verify/otp", { email, otp });
            Swal.fire({
                icon: 'success',
                text: "OTP verified successfully",
                customClass: {
                    popup: 'my-swal-popup',
                    title: 'my-swal-title',
                    htmlContainer: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button',
                }
            });
            navigate("/reset/password", { state: { email } });
        } catch (err) {
            const errorMessage = err.response?.data?.error || "OTP verification failed";
            setError(errorMessage);
        }
    };

    return (
        <>
            <LoggedOutNavbar />
            <div className="container mt-20">
                <div className="row justify-content-center">
                    <div className="col-md-6">
                        <div className="card shadow border-0">
                            <div className="card-body">
                                <h4 className="card-title text-center text-123458">Verify OTP</h4>
                                <form onSubmit={handleSubmit}>
                                    <div className="mb-3">
                                        <label className="form-label text-123458">Enter OTP</label>
                                        <input type="text" className="form-control" value={otp} onChange={(e) => setOtp(e.target.value)} required />
                                    </div>
                                    {error && <p className="text-danger text-center fw-bold">{error}</p>}
                                    <button type="submit" className="btn btn-123458 w-100">Verify OTP</button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}

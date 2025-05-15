import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import AuthBackground from "../components/AuthBackground";

export default function ForgotPassword() {

    const [formData, setFormData] = useState({ email: "" });
    const navigate = useNavigate();

    const resetForm = () => {
        setFormData({ email: "" });
    }

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const { data, status } = await axios.post("http://localhost:8081/user/forgot/password", formData);
            if (status === 200) {
                alert(data.message);
                navigate('/otp/verify', { state: { email: formData.email } });
            }
        } catch (err) {
            if (err.data?.error) {
                alert(`${err.data.error}`);
                console.log(err.data.error);
            } else {
                alert('OTP send failed due to server error.');
            }
        } finally {
            resetForm();
        }
    };

    return (
        <>
            <AuthBackground>

                <h3 className="mb-3">Forgot Password</h3>

                <p className="text-muted mb-3">Please enter your registered email to reset your password.</p>

                <form onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label className="text-6c757d" htmlFor="email">Email</label>
                        <input className="form-control" type="email" id="email" name="email" value={formData.email} onChange={handleChange} autoComplete="email" required />
                    </div>

                    <div className="mb-3">
                        <button type="submit" className="btn btn-dark w-100">Get OTP</button>
                    </div>
                </form>

            </AuthBackground>
        </>
    );
};